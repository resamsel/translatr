#!/usr/bin/env python
# -*- coding: utf-8 -*-

import argparse
import yaml
import requests

from collections import namedtuple

Locale = namedtuple('Locale', 'id name projectId')

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

if args.command == 'pull':
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
