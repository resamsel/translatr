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

from collections import namedtuple

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

Locale = namedtuple('Locale', 'id name projectId')

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


def eprint(msg, width=80):
	print('\n'.join(textwrap.wrap(msg.strip(), width)))


def init(args):
	try:
		with open('.translatr.yml', 'w') as f:
			f.write(textwrap.dedent("""
				translatr:
					endpoint: {endpoint}
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

	config.update(args.__dict__)

	return config


def info(args):
	pyaml.pprint({'translatr': read_config()})


def handle_http_error(response, config):
	try:
		raise Exception(API_ERROR.format(response.json()['error'], **config))
	except ValueError:
		raise Exception(API_HTML_ERROR.format(response.text))


def download(req, target):
	logger.debug('Download %s to %s', req.url, target)
	with open(target, 'wb') as f:
		for chunk in req.iter_content(chunk_size=1024):
			if chunk: # filter out keep-alive new chunks
				f.write(chunk)
	return


def assert_exists(d, key):
	path = ['translatr']
	for k in key.split('.'):
		path.append(k)
		if k not in d:
			raise Exception(CONFIG_KEY_MISSING.format('.'.join(path)))
		d = d[k]
		if d is None:
			raise Exception(CONFIG_KEY_EMPTY.format('.'.join(path)))

def pull(args):
	config = read_config_merge(args)

	assert_exists(config, 'endpoint')
	assert_exists(config, 'access_token')
	assert_exists(config, 'project_id')
	assert_exists(config, 'pull.target')
	assert_exists(config, 'pull.file_type')

	try:
		response = requests.get(
			'{endpoint}/api/project/{project_id}/locales'.format(**config),
			params={'access_token': config['access_token']}
		)
		response.raise_for_status()
	except requests.exceptions.ConnectionError as e:
		raise Exception(CONNECTION_ERROR.format(**config))
	except requests.exceptions.HTTPError:
		handle_http_error(response, config)

	locales = response.json()

	for loc in locales:
		locale = Locale(**loc)
		target = '{pull[target]}'.format(**config).format(locale=locale)
		if locale.name == 'default':
			target = re.sub(r'.\?default', '', target)
		else:
			target = target.replace('?', '')
		download(
			requests.get(
				'{endpoint}/api/locale/{0}/export/{pull[file_type]}'.format(
					locale.id,
					**config
				),
				params={'access_token': config['access_token']}
			),
			target
		)
		print('Downloaded {0} to {1}'.format(locale.name, target))


def target_repl(m):
	return m.group(0) \
		.replace('{', r'(?P<') \
		.replace('}', r'>\w*)') \
		.replace('.', '_')


def target_pattern(target):
	logger.debug('target_pattern(target=%s)', target)
	regex = re.sub(r'(\{[^}]*\})', target_repl, target)
	logger.debug('Regex: %s', regex)
	return re.compile(regex)


def push(args):
	config = read_config_merge(args)

	assert_exists(config, 'endpoint')
	assert_exists(config, 'access_token')
	assert_exists(config, 'project_id')
	assert_exists(config, 'push.target')
	assert_exists(config, 'push.file_type')

	try:
		response = requests.get(
			'{endpoint}/api/project/{project_id}/locales'.format(**config),
			params={'access_token': config['access_token']}
		)
		response.raise_for_status()
	except requests.exceptions.ConnectionError as e:
		raise Exception(CONNECTION_ERROR.format(**config))
	except requests.exceptions.HTTPError:
		handle_http_error(response, config)

	project = response.json()

	locales = dict([(loc['name'], Locale(**loc)) for loc in project])

	target = '{push[target]}'.format(**config)
	file_filter = re.sub(r'(.\?)?\{locale.name\}', r'*', target)
	logger.debug('Filter: %s', file_filter)
	pattern = target_pattern(target)
	for filename in glob.iglob(file_filter):
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
				# Create locale
				response = requests.post(
					'{endpoint}/api/locale'.format(**config),
					json={
						'access_token': config['access_token'],
						'project': {
							'id': config['project_id']
						},
						'name': localeName
					})
				response.raise_for_status()
				# Put response locale in locales
				locales[localeName] = Locale(**response.json())
				created = True
			except requests.exceptions.ConnectionError as e:
				eprint(CONNECTION_ERROR.format(**config))
			except requests.exceptions.HTTPError:
				try:
					handle_http_error(response, config)
				except BaseException as e:
					eprint(e.message)
			except ValueError as e:
				logger.exception(e)
			except BaseException as e:
				logger.exception(e)

		if localeName in locales:
			try:
				response = requests.post(
					'{endpoint}/api/locale/{locale.id}/import/{push[file_type]}'.format(
						locale=locales[localeName],
						**config),
					params={
						'access_token': config['access_token']
					},
					data={
						'fileType': config['push']['file_type']
					},
					files={
						'messages': open(filename, 'r')
					})
				response.raise_for_status()
				print(
					'Uploaded {0} to {1}{2}'.format(
						filename,
						localeName,
						{ True: ' (new)', False: ''}.get(created)
					)
				)
			except requests.exceptions.ConnectionError as e:
				eprint(CONNECTION_ERROR.format(**config))
			except requests.exceptions.HTTPError:
				try:
					handle_http_error(response, config)
				except BaseException as e:
					eprint(e.message)
		else:
			print('Could neither find nor create locale {0}'.format(localeName))


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

	subparsers = parser.add_subparsers(
		title="commands"
	)

	parser_init = subparsers.add_parser(
		'init',
		help='initialises the directory with a .translatr.yml file'
	)
	parser_init.add_argument(
		'endpoint',
		help='the URL to the Translatr endpoint'
	)
	parser_init.add_argument(
		'project_id',
		help='the ID of the Translatr project'
	)
	parser_init.add_argument(
		'--pull-file-type',
		default='play_messages',
		help='the format of the files to be downloaded'
	)
	parser_init.add_argument(
		'--pull-target',
		default='conf/messages.?{locale.name}',
		help='the location format of the downloaded files'
	)
	parser_init.add_argument(
		'--push-file-type',
		default='play_messages',
		help='the format of the files to be uploaded'
	)
	parser_init.add_argument(
		'--push-target',
		default='conf/messages.?{locale.name}',
		help='the location format of the uploaded files'
	)
	parser_init.set_defaults(func=init)

	parser_info = subparsers.add_parser(
		'info',
		help='show info about project'
	)
	parser_info.set_defaults(func=info)

	parser_push = subparsers.add_parser(
		'push',
		help='pushing sends matching messages files to the given endpoint, creating locales if needed'
	)
	parser_push.set_defaults(func=push)

	parser_pull = subparsers.add_parser(
		'pull',
		help='pulling downloads all locales into separate files into the configured files'
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
		eprint(e.args[0])
	except BaseException as e:
		logger.exception(e)
		eprint(str(e))

main()
