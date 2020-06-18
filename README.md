# Translatr [![Build Status](https://travis-ci.org/resamsel/translatr.svg?branch=master)](https://travis-ci.org/resamsel/translatr) [![Coverage Status](https://coveralls.io/repos/github/resamsel/translatr/badge.svg?branch=master)](https://coveralls.io/github/resamsel/translatr?branch=master) [![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=flat)](http://makeapullrequest.com)

A modern and intuitive i18n tool. Translatr simplifies i18n for developers and translators. It uses the [Play Framework](http://www.playframework.com) to translate other projects. Importing conf/messages.locale (Play Framework), src/main/resources/messages_locale.properties (Java properties), and i18n/locale/main.po (Gettext) files allows easy locale management.

[Demo](https://translatr.repanzar.com/)

![Project Overview Example](doc/images/dashboard.png "Dashboard Example")

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
  endpoint: https://translatr.repanzar.com
  access_token: $ACCESS_TOKEN
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

## Developing

### Prerequisites

For development you'll need the following dependencies.

1. Java 8 JDK (i.e. OpenJDK 8)
1. Docker
1. Docker Compose
1. Node.js

### Using IntelliJ IDEA

1. Run the compile SBT task

   ```
   bin/activator compile
   ```

1. Import project in IntelliJ IDEA as a _Scala_ project
1. Run the run configuration named _Start Database_
1. Run the run configuration named _Start Server_
1. Go to [localhost:4210/ui](http://localhost:4210/ui) to see it running

### Running it Manually

1. Run the compile SBT task

   ```
   bin/activator compile
   ```

1. Run database container by using _docker compose_

   ```
   export POSTGRES_PASSWORD=translatr
   docker-compose up
   ```

1. Run development server

   ```
   export AUTH_PROVIDERS=keycloak
   export KEYCLOAK_CLIENT_ID=translatr-localhost
   export KEYCLOAK_CLIENT_SECRET=$YOUR_KEYCLOAK_CLIENT_SECRET
   export REDIRECT_BASE=http://localhost:4210
   bin/activator ~run -Dconfig.file=dev.conf
   ```

1. Go to [localhost:4210/ui](http://localhost:4210/ui) to see it running

## Contributing

Information about contributing to this project can be found on the
[CONTRIBUTING](CONTRIBUTING.md) page.
