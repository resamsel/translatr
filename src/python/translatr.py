#!/usr/bin/env python
# -*- coding: utf-8 -*-

import argparse
import yaml
import pyaml
import requests
import glob
import logging
import re
import sys
import os
import textwrap

from argparse import Namespace
from tabulate import tabulate
from uuid import UUID

reload(sys)
sys.setdefaultencoding('utf-8')

# define the regex pattern that the parser will use to 'implicitly' tag your node
ENV_VAR_PATTERN = re.compile(r'^(.*?)\$\{(\?)?([^\}]*)\}(.*?)$')

TRANSLATR_YML_NOT_FOUND = textwrap.dedent("""
	Could not find .translatr.yml: initialise with `translatr init`
""")
CONFIG_KEY_MISSING = textwrap.dedent("""
	Error in .translatr.yml: could not find key "{0}"
""")
CONFIG_KEY_EMPTY = textwrap.dedent("""
	Error in .translatr.yml: key "{0}" is empty
""")
CONNECTION_ERROR = textwrap.dedent("""
	Connection to {endpoint} could not be established, please check
	your .translatr.yml config (translatr.endpoint)
""")
API_ERROR = textwrap.dedent("""
	{0} - please check your .translatr.yml config
""")
API_HTML_ERROR = textwrap.dedent("""
	An undefined error occurred while talking to the API:

	{0}
""")


logger = logging.getLogger(__name__)


def replace_envvar(value):
  match = ENV_VAR_PATTERN.match(value)
  if match is not None:
    prefix, optional, env_var, suffix = match.groups()
    if env_var in os.environ:
      return prefix + os.environ[env_var] + replace_envvar(suffix)
    if optional is None:
      raise Exception('Environment variable {0} is not set'.format(env_var))
    return prefix + replace_envvar(suffix)
  return value


def envvar_constructor(loader, node):
  value = loader.construct_scalar(node)
  return replace_envvar(value)


def eprint(msg, width=None):
	if width is None:
		_, width = [int(i) for i in os.popen('stty size', 'r').read().split()]
	print('\n'.join(textwrap.wrap(msg.strip(), width)))


def target_repl(m):
	return m.group(0) \
		.replace('.', '_') \
		.replace('{', r'(?P<') \
		.replace('}', r'>.*)')


def target_pattern(target):
	logger.debug('target_pattern(target=%s)', target)
	regex = re.sub(r'(\{[^}]*\})', target_repl, target)
	logger.debug('Regex: %s', regex)
	return re.compile(regex)


def handle_http_error(response, config):
	logger.debug('Handling API error: %s', response.text)

	try:
		json = response.json()
		if response.status_code == 400:
			raise Exception('{0}: {1}'.format(
				json['error']['message'],
				', '.join([
					'{message} ({field})'.format(**v)
					for v in json['error']['violations']
				])
			))
		raise Exception(json['error']['message'])
	except ValueError:
		raise Exception(API_HTML_ERROR.format(response.text))


def download(res, target):
	logger.debug('Download %s to %s', res.url, target)

	with open(target, 'wb') as f:
		for chunk in res.iter_content(chunk_size=1024):
			if chunk: # filter out keep-alive new chunks
				f.write(chunk)
	return


def assert_exists(config, *keys):
	for key in keys:
		d = config
		path = ['translatr']
		for k in key.split('.'):
			path.append(k)
			if k not in d:
				raise Exception(CONFIG_KEY_MISSING.format('.'.join(path)))
			d = d[k]
			if d is None:
				raise Exception(CONFIG_KEY_EMPTY.format('.'.join(path)))


class Request(object):
	def __init__(self, config):
		self.config = config

	def request(self, operation, path, params=None, **kwargs):
		assert_exists(self.config, 'endpoint', 'access_token')

		if params is None:
			params = {}
		params.update({'access_token': self.config['access_token']})

		try:
			response = operation(
				'{endpoint}/api/{path}'.format(path=path, **self.config),
				params=params,
				**kwargs
			)
			response.raise_for_status()
		except requests.exceptions.ConnectionError as e:
			raise Exception(CONNECTION_ERROR.format(**self.config))
		except requests.exceptions.HTTPError:
			handle_http_error(response, self.config)

		return response

	def get(self, path, **kwargs):
		return self.request(requests.get, path, **kwargs)


	def post(self, path, **kwargs):
		return self.request(requests.post, path, **kwargs)


	def put(self, path, **kwargs):
		return self.request(requests.put, path, **kwargs)


	def delete(self, path, **kwargs):
		return self.request(requests.delete, path, **kwargs)


Project, Locale, Key, User = Namespace, Namespace, Namespace, Namespace


class Api(object):
	def __init__(self, config):
		self.config = config
		self.request = Request(config)

	def projects(self, **kwargs):
		return [
			Project(**p)
			for p in self.request.get('projects', **kwargs).json()['list']
		]

	def project_create(self, json):
		return Project(**self.request.post('project', json=json).json())

	def project_delete(self, project_id):
		return Project(
			**self.request.delete('project/{0}'.format(project_id)).json()
		)

	def locales(self, **kwargs):
		assert_exists(self.config, 'project_id')
		return [
			Locale(**l)
			for l in self.request.get(
					'locales/{project_id}'.format(**self.config),
					**kwargs
				).json()['list']
		]

	def locale_create(self, json):
		return Locale(**self.request.post('locale', json=json).json())

	def locale_delete(self, locale_id):
		return Locale(
			**self.request.delete('locale/{0}'.format(locale_id)).json()
		)

	def locale_import(self, locale_id, file_type, files=None):
		return self.request.post(
			'locale/{0}/import'.format(locale_id),
			data={'fileType': file_type},
			files=files
		)

	def locale_export(self, locale_id, file_type):
		return self.request.get(
			'locale/{0}/export/{1}'.format(locale_id, file_type),
			stream=True
		)

	def keys(self, **kwargs):
		assert_exists(self.config, 'project_id')
		return [
			Key(**k)
			for k in self.request.get(
					'keys/{project_id}'.format(**self.config),
					**kwargs
				).json()['list']
		]

	def key_create(self, json):
		return Key(**self.request.post('key', json=json).json())

	def key_delete(self, key_id):
		return Key(
			**self.request.delete('key/{0}'.format(key_id)).json()
		)

	def users(self, **kwargs):
		return [
			User(**u)
			for u in self.request.get(
					'users',
					**kwargs
				).json()['list']
		]


def read_config():
	# Define a custom tag and associate the regex pattern we defined
	yaml.add_implicit_resolver("!envvar", ENV_VAR_PATTERN)
	# 'register' the constructor so that the parser will invoke 'envvar_constructor' for each node '!pathex'
	yaml.add_constructor('!envvar', envvar_constructor)
	try:
		with open('.translatr.yml') as f:
			return yaml.load(f)['translatr']
	except IOError as e:
		raise Exception(TRANSLATR_YML_NOT_FOUND)


def read_config_merge(args):
	config = read_config()
	if config is None:
		raise Exception(CONFIG_KEY_MISSING.format('translatr'))

	params = dict((k,v) for k,v in args.__dict__.iteritems() if v is not None)
	config.update(params)

	if 'endpoint' in config:
		config['endpoint'] = config['endpoint'].strip('/')

	return config


def init(args):
	try:
		with open('.translatr.yml', 'w') as f:
			f.write(textwrap.dedent("""
				translatr:
					endpoint: {endpoint}
					access_token: {access_token}
					project_id: {project_id}
					push:
						file_type: {push_file_type}
						target: {push_target}
					pull:
						file_type: {pull_file_type}
						target: {pull_target}
			""").strip().format(**args.__dict__).replace('\t', '  '))
		print('Config initialised into .translatr.yml')
	except IOError as e:
		logger.exception(e)
		print(e)


def config_info(args):
	pyaml.pprint({'translatr': read_config()})


def projects(args):
	config = read_config_merge(args)

	projects = Api(config).projects(params={'search': args.search})

	print(tabulate(
		[(p.id, p.name, p.ownerName) for p in projects],
		tablefmt="plain"
	))


def create_project(args):
	config = read_config_merge(args)

	project = Api(config).project_create({'name': args.project_name})

	print('Project {0} has been created (ID: {1})'.format(
		project.name,
		project.id
	))


def remove_project(args):
	config = read_config_merge(args)

	api = Api(config)
	for project_id in args.project_ids:
		try:
			UUID(project_id, version=4)
		except ValueError:
			projects = [
				p
				for p in api.projects(params={'search': project_id})
				if p.name == project_id
			]
			if projects:
				project_id = projects[0].id
			else:
				raise Exception(
					"Project with ID '{0}' not found".format(project_id))

		project = api.project_delete(project_id)

		print(
			'Project {0} has been deleted'.format(
				project.name.replace(
					'{0}-'.format(project_id),
					''
				)
			)
		)


def locales(args):
	config = read_config_merge(args)

	locales = Api(config).locales(params={'search': args.search})

	print(tabulate([(l.id, l.name) for l in locales], tablefmt="plain"))


def create_locale(args):
	config = read_config_merge(args)

	assert_exists(config, 'project_id')

	locale = Api(config).locale_create({
		'projectId': config['project_id'],
		'name': args.locale_name
	})

	print('Locale {0} has been created'.format(locale.name))


def remove_locale(args):
	config = read_config_merge(args)

	api = Api(config)
	for locale_id in args.locale_ids:
		try:
			UUID(locale_id, version=4)
		except ValueError:
			locales = [
				l
				for l in api.locales(params={'search': locale_id})
				if l.name == locale_id
			]
			if locales:
				locale_id = locales[0].id
			else:
				raise Exception(
					"Locale with ID '{0}' not found".format(locale_id))

		locale = api.locale_delete(locale_id)

		print('Locale {0} has been deleted'.format(locale.name))


def keys(args):
	config = read_config_merge(args)

	keys = Api(config).keys(params={'search': args.search})

	print(tabulate([(k.id, k.name) for k in keys], tablefmt="plain"))


def create_key(args):
	config = read_config_merge(args)

	key = Api(config).key_create({
		'projectId': config['project_id'],
		'name': args.key_name
	})

	print('Key {0} has been created'.format(key.name))


def remove_key(args):
	config = read_config_merge(args)

	api = Api(config)
	for key_id in args.key_ids:
		try:
			UUID(key_id, version=4)
		except ValueError:
			keys = [
				k
				for k in api.keys(params={'search': key_id})
				if k.name == key_id
			]
			if keys:
				key_id = keys[0].id
			else:
				raise Exception(
					"Key with ID '{0}' not found".format(key_id))

		key = api.key_delete(key_id)

		print('Key {0} has been deleted'.format(key.name))


def users(args):
	config = read_config_merge(args)

	users = Api(config).users(params={'search': args.search})

	print(tabulate([(u.id, u.name, u.username) for u in users], tablefmt="plain"))


def pull(args):
	config = read_config_merge(args)

	assert_exists(config, 'pull.target', 'pull.file_type')

	api = Api(config)
	for locale in api.locales():
		target = '{pull[target]}'.format(**config).format(locale=locale)
		if locale.name == 'default':
			target = re.sub(r'.\?default', '', target)
		else:
			target = target.replace('?', '')

		download(
			api.locale_export(locale.id, config['pull']['file_type']),
			target
		)

		print('Downloaded {0} to {1}'.format(locale.name, target))


def push(args):
	config = read_config_merge(args)

	assert_exists(config, 'push.target', 'push.file_type')

	api = Api(config)
	locales = dict([(l.name, l) for l in api.locales()])

	target = '{push[target]}'.format(**config)
	file_filter = re.sub(r'(.\?)?\{locale.name\}', r'*', target)
	logger.debug('Filter: %s', file_filter)
	pattern = target_pattern(target)
	for filename in glob.iglob(file_filter):
		logger.debug('Filename: %s', filename)
		m = pattern.match(filename)
		if not m:
			# Skip this entry
			print('Filename {0} does not match target: {1}'.format(filename, target))
			continue
		groups = m.groupdict()
		logger.debug('Groups: %s', groups)

		localeName = ''
		if 'locale_name' in groups:
			localeName = groups['locale_name']
		if localeName == '':
			localeName = 'default'

		logger.debug('Locale name: %s', localeName)

		created = False
		if localeName not in locales:
			try:
				# Create locale and put it in locales
				locales[localeName] = api.locale_create({
					'projectId': config['project_id'],
					'name': localeName
				})
				created = True
			except BaseException as e:
				# Exception is OK, but log it
				logger.exception(e)

		if localeName in locales:
			locale = locales[localeName]
			try:
				api.locale_import(
					locale.id,
					config['push']['file_type'],
					files={'messages': open(filename, 'r')}
				)
				print(
					'Uploaded {0} to {1}{2}'.format(
						filename,
						localeName,
						{ True: ' (new)', False: ''}.get(created)
					)
				)
			except BaseException as e:
				# Exception is OK, but log and print it
				logger.exception(e)
				eprint(e.message)
		else:
			print('Could neither find nor create locale {0}'.format(localeName))


def create_parser_init(subparsers):
	parser_init = subparsers.add_parser(
		'init',
		help='initialises the directory with a .translatr.yml file'
	)
	parser_init.add_argument(
		'endpoint',
		help='the URL to the Translatr endpoint'
	)
	parser_init.add_argument(
		'access_token',
		help='the access token for API calls'
	)
	parser_init.add_argument(
		'project_id',
		help='the ID of the Translatr project'
	)
	parser_init.add_argument(
		'--pull-file-type',
		default='play_messages',
		help='the format of the files to be downloaded (default: %(default)s)'
	)
	parser_init.add_argument(
		'--pull-target',
		default='conf/messages.?{locale.name}',
		help='the location format of the downloaded files (default: %(default)s)'
	)
	parser_init.add_argument(
		'--push-file-type',
		default='play_messages',
		help='the format of the files to be uploaded (default: %(default)s)'
	)
	parser_init.add_argument(
		'--push-target',
		default='conf/messages.?{locale.name}',
		help='the location format of the uploaded files (default: %(default)s)'
	)
	parser_init.set_defaults(func=init)


def create_parser_project(subparsers):
	parser_project = subparsers.add_parser(
		'project',
		help='project commands'
	)
	subparsers_project = parser_project.add_subparsers(
		title="commands"
	)
	parser_project_list = subparsers_project.add_parser(
		'ls',
		help='list projects'
	)
	parser_project_list.set_defaults(func=projects)
	parser_project_list.add_argument(
		'search',
		nargs='?',
		help='the search string'
	)

	parser_project_create = subparsers_project.add_parser(
		'create',
		help='create project'
	)
	parser_project_create.set_defaults(func=create_project)
	parser_project_create.add_argument(
		'project_name',
		help='the project name'
	)

	parser_project_remove = subparsers_project.add_parser(
		'rm',
		help='remove projects'
	)
	parser_project_remove.set_defaults(func=remove_project)
	parser_project_remove.add_argument(
		'project_ids',
		nargs='+',
		help='the project IDs'
	)


def create_parser_locale(subparsers):
	parser_locale = subparsers.add_parser(
		'locale',
		help='locale commands'
	)
	subparsers_locale = parser_locale.add_subparsers(
		title="commands"
	)
	parser_locale_list = subparsers_locale.add_parser(
		'ls',
		help='list locales'
	)
	parser_locale_list.set_defaults(func=locales)
	parser_locale_list.add_argument(
		'search',
		nargs='?',
		help='the search string'
	)
	parser_locale_list.add_argument(
		'-p',
		'--project-id',
		help='the project ID'
	)

	parser_locale_create = subparsers_locale.add_parser(
		'create',
		help='create locale'
	)
	parser_locale_create.set_defaults(func=create_locale)
	parser_locale_create.add_argument(
		'locale_name',
		help='the locale name'
	)
	parser_locale_create.add_argument(
		'-p',
		'--project-id',
		help='the project ID'
	)

	parser_locale_remove = subparsers_locale.add_parser(
		'rm',
		help='remove locales'
	)
	parser_locale_remove.set_defaults(func=remove_locale)
	parser_locale_remove.add_argument(
		'locale_ids',
		nargs='+',
		help='the locale IDs'
	)
	parser_locale_remove.add_argument(
		'-p',
		'--project-id',
		help='the project ID'
	)


def create_parser_key(subparsers):
	parser_key = subparsers.add_parser(
		'key',
		help='key commands'
	)
	subparsers_key = parser_key.add_subparsers(
		title="commands"
	)
	parser_key_list = subparsers_key.add_parser(
		'ls',
		help='list keys'
	)
	parser_key_list.set_defaults(func=keys)
	parser_key_list.add_argument(
		'search',
		nargs='?',
		help='the search string'
	)
	parser_key_list.add_argument(
		'-p',
		'--project-id',
		help='the project ID'
	)

	parser_key_create = subparsers_key.add_parser(
		'create',
		help='create key'
	)
	parser_key_create.set_defaults(func=create_key)
	parser_key_create.add_argument(
		'key_name',
		help='the key name'
	)
	parser_key_create.add_argument(
		'-p',
		'--project-id',
		help='the project ID'
	)

	parser_key_remove = subparsers_key.add_parser(
		'rm',
		help='remove keys'
	)
	parser_key_remove.set_defaults(func=remove_key)
	parser_key_remove.add_argument(
		'key_ids',
		nargs='+',
		help='the key IDs'
	)
	parser_key_remove.add_argument(
		'-p',
		'--project-id',
		help='the project ID'
	)


def create_parser_user(subparsers):
	parser_user = subparsers.add_parser(
		'user',
		help='user commands'
	)
	subparsers_user = parser_user.add_subparsers(
		title="commands"
	)
	parser_user_list = subparsers_user.add_parser(
		'ls',
		help='list users'
	)
	parser_user_list.set_defaults(func=users)
	parser_user_list.add_argument(
		'search',
		nargs='?',
		help='the search string'
	)


def create_parser():
	parser = argparse.ArgumentParser(
		description='Command line interface for translatr.'
	)
	parser.add_argument(
		'-L',
		'--logfile',
		type=argparse.FileType('a'),
		default='/tmp/translatr.log',
		help='the file to log to'
	)
	parser.add_argument(
		'--debug',
		dest='loglevel',
		action='store_const',
		const=logging.DEBUG,
		default=logging.WARN,
		help='set loglevel to debug'
	)
	parser.add_argument(
		'-e',
		'--endpoint',
		help='the URL to the Translatr endpoint (default: from .translatr.yml)'
	)
	parser.add_argument(
		'-t',
		'--access-token',
		help='the access token to be used (default: from .translatr.yml)'
	)

	subparsers = parser.add_subparsers(
		title="commands"
	)

	create_parser_init(subparsers)
	create_parser_project(subparsers)
	create_parser_locale(subparsers)
	create_parser_key(subparsers)
	create_parser_user(subparsers)

	parser_config = subparsers.add_parser(
		'config',
		help='show info about configuration'
	)
	parser_config.set_defaults(func=config_info)

	parser_push = subparsers.add_parser(
		'push',
		help='pushing sends matching messages files to the given endpoint, creating locales if needed'
	)
	parser_push.set_defaults(func=push)

	parser_pull = subparsers.add_parser(
		'pull',
		help='pulling downloads all locales into configured locations'
	)
	parser_pull.set_defaults(func=pull)

	return parser


def main():
	parser = create_parser()

	if len(sys.argv) < 2:
		parser.print_help()
		sys.exit(1)

	args = parser.parse_args()

	logging.basicConfig(
		stream=args.logfile,
		level=args.loglevel,
		format="%(asctime)s %(levelname)s %(name)s: %(message)s",
		datefmt="%Y-%m-%d %H:%M:%S"
	)

	try:
		args.func(args)
	except Exception as e:
		logger.exception(e)
		eprint(e.args[0])
	except BaseException as e:
		logger.exception(e)
		eprint(str(e))

main()
