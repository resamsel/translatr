Translatr
=========

[![Build Status](https://travis-ci.org/resamsel/translatr.svg?branch=main)](https://travis-ci.org/resamsel/translatr)
[![Coverage Status](https://coveralls.io/repos/github/resamsel/translatr/badge.svg?branch=main)](https://coveralls.io/github/resamsel/translatr?branch=main)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=flat)](http://makeapullrequest.com)

A modern and intuitive i18n tool. Translatr simplifies i18n for developers and translators. It uses
the [Play Framework](http://www.playframework.com) to translate other projects. Importing
`conf/messages.locale` (Play Framework), `src/main/resources/messages_locale.properties` (Java
properties), and `i18n/locale/main.po` (Gettext) files allows easy locale management.

[Demo](https://translatr.repanzar.com/)

![Dashboard Example](doc/images/dashboard.png "Dashboard Example")

## Speeds up development

Preview, quick switching between locales, and the possibility of build system integration allows
faster development.

![Language Editor](doc/images/language-editor.png "Language Editor")

## User experience focused

Using the application should feel as comfortable as possible because of focusing on both main use
cases - development and translation.

![Project Example](doc/images/project.png "Project Example")

## Easy to work with

All elements - search, content creation, ... - follow the concepts of Material
Design to allow the easiest and most intuitive usage possible.

![User Settings](doc/images/user-settings.png "User Settings")

# Command Line Interface

The Command Line Interface allows managing translations through the command line. Operations
can be used in build scripts to automatically retrieve the latest translations.

## Installation

Paste this at a terminal prompt:

```
curl -fsSL https://raw.githubusercontent.com/resamsel/translatr/main/install.sh | bash
```

## Usage

To use Translatr CLI we need a `.translatr.yml` config file. Create one in any directory that you
want to enable the CLI. The Translatr CLI will then use that file as configuration.

Creating a config file is easy with version 3. On the project page, scroll down to the CLI
Integration card. You'll find a sample file, along with the option of choosing the file type and
the access token.

![CLI Integration Card](doc/images/project-cli-integration.png "CLI Integration Card")

### Pushing

By pushing you send the matching messages files to the given endpoint, creating locales if needed.

```
translatr push
```

This will overwrite all existing messages, if any. No existing keys/locales will be removed, this is not a sync operation.

### Pulling

By pulling you download all locales into separate files into the configured files (translatr.pull.target key).

```
translatr pull
```

This will overwrite any existing files locally. All known locales will be downloaded.

## Development and Contributing

Information about contributing to this project can be found on the
[CONTRIBUTING](CONTRIBUTING.md) page.

## Resources

SVG Icons from: https://www.svgrepo.com/collection/soft-colored-ui-icons/
