# Translatr [![Build Status](https://travis-ci.org/resamsel/translatr.svg?branch=master)](https://travis-ci.org/resamsel/translatr) [![Coverage Status](https://coveralls.io/repos/github/resamsel/translatr/badge.svg?branch=master)](https://coveralls.io/github/resamsel/translatr?branch=master)

A modern and intuitive i18n tool. Translatr simplifies i18n for developers and translators. It uses the [Play Framework](http://www.playframework.com) to translate other projects. Importing conf/messages.locale (Play Framework), src/main/resources/messages_locale.properties (Java properties), and i18n/locale/main.po (Gettext) files allows easy locale management.

[Demo Installation](http://translatr.resamsel.com/)

![Project Overview Example](https://github.com/resamsel/translatr/wiki/images/project.png "Project Overview Example")

## Speeds up development

Preview, quick switching between locales, and the possibility of build system integration allows faster development.

## User experience focused

Using the application is made as comfortable as possible because of focussing on both main use cases - development and translation.

## Easy to work with

All elements - search, content creation, notifications, ... - follow the concepts of Material Design to allow the easiest and most intuitive use possible.

# Command Line Interface

The Command Line Interface allows to manage translations through the command line. Operations can be used in build scripts to automatically retrieve the latest translations.

## Installation

Paste this at a terminal prompt:

```
curl -fsSL https://raw.githubusercontent.com/resamsel/translatr/master/install.sh | bash
```

## Usage

To use Translatr CLI we need a .translatr.yml config file. Create one in any directory that you want to enable the CLI. Translatr will then use that file as configuration.

### Example .translatr.yml for Play messages

```
translatr:
  endpoint: http://localhost:9000
  access_token: <access token>
  project_id: <project ID>
  pull:
    file_type: play_messages
    target: conf/messages.?{locale.name}
  push:
    file_type: play_messages
    target: conf/messages.?{locale.name}
```

Create a new access token (profile -> access tokens) and enter it in .translatr.yml.

![User Access Token](https://github.com/resamsel/translatr/wiki/images/user-access-token.png "User Access Token")

Then, create a project in the web GUI and enter the project ID (40 character UUID in the URL, i.e. http://localhost:9000/project/**6f1ee0a7-b5d1-4c7f-8e4d-46c09fece220** -> project ID: **6f1ee0a7-b5d1-4c7f-8e4d-46c09fece220**) in .translatr.yml.

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

# Contributing

Any suggestion or issue is welcome. Push request are very welcome, please check out the guidelines.

## Pull Request Guidelines

1. Pull requests should link to at least one issue that it is solving. The issue itself should be descriptive so that a reviewer can understand what the PR is doing. The description in the PR should only add any useful additional information needed that is not in the issue.
1. Keep pull requests small and simple. A pull request should generally address one, or a few related issues. This makes it easier to review the pull request, and document the changes.
1. Test your code before submitting a pull request! It is unsafe to merge untested code even if you think it will work without testing. Creating unit tests for any changes is a good idea, and is highly encouraged.
1. Before issuing a pull request, sync with the main repository and address any conflicts that may exist.

# Development

The project can be developed with Eclipse, needs a PostgreSQL database and a Redis key/value store. For authorisation, any of the following is implemented: Google, GitHub, Facebook, Twitter, and Keycloak.

Creating project files for Eclipse:

```
bin/activator eclipse
```

## Style Guide

Use the Google style guide for Java:

https://raw.githubusercontent.com/google/styleguide/gh-pages/eclipse-java-google-style.xml

## Authorisation

### Google

Credentials can be retrieved here: https://code.google.com/apis/console

```
export AUTH_PROVIDERS=google
export GOOGLE_CLIENT_ID=...
export GOOGLE_CLIENT_SECRET=...
```

### GitHub

Credentials can be retrieved here: https://github.com/settings/applications/new

```
export AUTH_PROVIDERS=github
export GITHUB_CLIENT_ID=...
export GITHUB_CLIENT_SECRET=...
```

### Facebook

Credentials can be retrieved here: https://developers.facebook.com/apps

```
export AUTH_PROVIDERS=facebook
export FACEBOOK_CLIENT_ID=...
export FACEBOOK_CLIENT_SECRET=...
```

### Twitter

Credentials can be retrieved here: https://dev.twitter.com/docs/auth/oauth

```
export AUTH_PROVIDERS=twitter
export TWITTER_CONSUMER_KEY=...
export TWITTER_CONSUMER_SECRET=...
```

### Keycloak

The realm needs to be named Translatr. This is a sample [Keycloak app for Heroku](https://github.com/resamsel/keycloak-swarm-heroku).

```
export KEYCLOAK_HOST=http://localhost:8080
export KEYCLOAK_CLIENT_ID=...
export KEYCLOAK_CLIENT_SECRET=...
```

## Running Translatr

Start database and Redis (Docker/docker-compose needed):

```
docker-compose up -d
```

Then, start the application:

```
bin/activator ~run -Dconfig.file=dev.conf
```

## Testing

Unit and integration tests live in the **test/** directory. In Eclipse, this
directory should be a source directory.

Unit tests can be run by issuing the following command:

```
bin/activator clean jacoco:cover -Dconfig.file=test.conf -J-Xmx1g
```

Jacoco generates a coverage report in **target/scala-2.11/jacoco/html/index.html**
