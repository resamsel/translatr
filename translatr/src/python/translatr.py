#!/usr/bin/env python
# -*- coding: utf-8 -*-

import argparse
import yaml
import requests
import glob
import logging
import re

from collections import namedtuple

Locale = namedtuple('Locale', 'id version name projectId')

logger = logging.getLogger(__name__)

parser = argparse.ArgumentParser(description='Command line interface for play-translatr.')
parser.add_argument(
	'command',
	help='the command to invoke; options: pull'
)
parser.add_argument(
	'-L',
	'--logfile',
	type=argparse.FileType('ab'),
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

config = yaml.load(file('.translatr.yml'))['translatr']

args = parser.parse_args()

config.update(args.__dict__)

logging.basicConfig(
	stream=args.logfile,
	level=args.loglevel,
	format="%(asctime)s %(levelname)s %(name)s: %(message)s",
	datefmt="%Y-%m-%d %H:%M:%S"
)

def download(url, target):
	logger.debug('Download %s to %s', url, target)
	req = requests.get(url)
	with open(target, 'wb') as f:
		for chunk in req.iter_content(chunk_size=1024):
			if chunk: # filter out keep-alive new chunks
				f.write(chunk)
	return


def help():
	print 'help'


def pull():
	response = requests.get(
		'{endpoint}/api/locales/{project_id}'.format(**config))
	locales = response.json()

	for loc in locales:
		locale = Locale(**loc)
		target = '{pull[target]}'.format(**config).format(locale=locale)
		if locale.name == 'default':
			target = re.sub(r'.\?default', '', target)
		else:
			target = target.replace('?', '')
		download(
			'{endpoint}/locale/{0}/export/{pull[file_type]}'.format(
				locale.id,
				**config
			),
			target
		)
		print 'Downloaded {0} to {1}'.format(locale.name, target)


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


def push():
	response = requests.get(
		'{endpoint}/api/locales/{project_id}'.format(**config))
	project = response.json()

	locales = dict([(loc['name'], Locale(**loc)) for loc in project])

	target = '{push[target]}'.format(**config)
	file_filter = re.sub(r'(.\?)\{locale.name\}', r'*', target)
	logger.debug('Filter: %s', file_filter)
	pattern = target_pattern(target)
	for filename in glob.iglob(file_filter):
		m = pattern.match(filename)
		if not m:
			# Skip this entry
			print 'Filename {0} does not match target: {1}'.format(filename, target)
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
				response = requests.put(
					'{endpoint}/api/locale'.format(**config),
					json={
						'project': {
							'id': config['project_id']
						},
						'name': localeName
					})
				# Put response locale in locales
				locales[localeName] = Locale(**response.json())
				created = True
			except ValueError as e:
				logger.exception(e)
			except BaseException as e:
				logger.exception(e)

		if localeName in locales:
			requests.post(
				'{endpoint}/locale/{locale.id}/import'.format(
					locale=locales[localeName],
					**config),
				data={
					'fileType': config['push']['file_type']
				},
				files={
					'messages': open(filename, 'r')
				})
			print 'Uploaded {0} to {1}{2}'.format(
				filename,
				localeName,
				{ True: ' (new)', False: ''}.get(created)
			)
		else:
			print 'Could neither find nor create locale {0}'.format(localeName)
{
	'pull': pull,
	'push': push
}.get(args.command, help)()
