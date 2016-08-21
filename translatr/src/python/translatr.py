#!/usr/bin/env python
# -*- coding: utf-8 -*-

import argparse
import yaml
import requests
import glob
import logging

from collections import namedtuple

Locale = namedtuple('Locale', 'id name projectId')

logger = logging.getLogger(__name__)

parser = argparse.ArgumentParser(description='Command line interface for play-translatr.')
parser.add_argument(
	'command',
	help='the command to invoke; options: pull'
)

config = yaml.load(file('.translatr.yml'))['translatr']

args = parser.parse_args()

config.update(args.__dict__)

def download(url, target):
#	print 'Download {0} to {1}'.format(url, target)
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
			target = target.replace('.default', '', 1)
		download(
			'{endpoint}/locale/{0}/export'.format(locale.id, **config),
			target
		)
		print 'Downloaded {0} to {1}'.format(locale.name, target)

def push():
	response = requests.get(
		'{endpoint}/api/locales/{project_id}'.format(**config))
	locales = dict([(loc['name'], Locale(**loc)) for loc in response.json()])

	target = '{push[target]}'.format(**config)
	prefix = target.replace('.{locale.name}', '')
	for filename in glob.iglob(prefix + '*'):
		localeName = filename.replace(prefix, '')
		if localeName == '':
			localeName = 'default'
		else:
			localeName = localeName[1:]

		created = False
		if localeName not in locales:
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
			try:
				locales[localeName] = Locale(**response.json())
				created = True
			except BaseException as e:
				logger.exception(e)

		if localeName in locales:
			requests.post(
				'{endpoint}/locale/{locale.id}/import'.format(
					locale=locales[localeName],
					**config),
				files={
					'messages': open(filename, 'r')
				})
			print 'Uploaded {0} to {1}{2}'.format(
				filename,
				localeName,
				{ True: ' (new)', False: ''}.get(created)
			)

{
	'pull': pull,
	'push': push
}.get(args.command, help)()
