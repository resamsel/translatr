# Contributing

You want to help out on Translatr? Great, feel welcome and read the following
sections in order to know what and how to work on something.

1. [How to file a bug report](#how-to-file-a-bug-report)
1. [How to suggest a new feature](#how-to-suggest-a-new-feature)
1. [How to set up your environment and run tests](#how-to-set-up-your-environment-and-run-tests)
1. [Pull request guidelines](#pull-request-guidelines)

## How to file a bug report

If you find a security vulnerability, do NOT open an issue. Email XXXX instead.

When filing an issue, make sure to answer these four questions:

1. What version of Translatr are you using?
1. What did you do?
1. What did you expect to see?
1. What did you see instead?

## How to suggest a new feature

If you find yourself wishing for a feature that doesn't exist in Translatr, you
are probably not alone. There are bound to be others out there with similar
needs. Many of the features that Translatr has today have been added because our
users saw the need. Open an issue on our issues list on GitHub which describes
the feature you would like to see, why you need it, and how it should work.

## How to set up your environment and run tests

The project can be developed with Eclipse, needs a PostgreSQL database and a
Redis key/value store. For authorisation, the following options are available:
Google, GitHub, Facebook, Twitter, and Keycloak.

Creating project files for Eclipse:

```
bin/activator eclipse
```

### Authorisation

To be able to use Translatr and create a user you need to authorise yourself.
This can be done in many ways, but you need at least one auth provider.

#### Google

Credentials can be retrieved from the [Google Cloud Resource Manager page](https://code.google.com/apis/console).

```
export AUTH_PROVIDERS=google
export GOOGLE_CLIENT_ID=...
export GOOGLE_CLIENT_SECRET=...
```

#### GitHub

Credentials can be retrieved from the [Register a new OAuth application page](https://github.com/settings/applications/new).

```
export AUTH_PROVIDERS=github
export GITHUB_CLIENT_ID=...
export GITHUB_CLIENT_SECRET=...
```

#### Facebook

Credentials can be retrieved from the [Facebook for developers page](https://developers.facebook.com/apps).

```
export AUTH_PROVIDERS=facebook
export FACEBOOK_CLIENT_ID=...
export FACEBOOK_CLIENT_SECRET=...
```

#### Twitter

Credentials can be retrieved from the [Twitter Developer Documentation on OAuth](https://dev.twitter.com/docs/auth/oauth).

```
export AUTH_PROVIDERS=twitter
export TWITTER_CONSUMER_KEY=...
export TWITTER_CONSUMER_SECRET=...
```

#### Keycloak

The realm needs to be named Translatr. This is a sample [Keycloak app for Heroku](https://github.com/resamsel/keycloak-swarm-heroku).

```
export KEYCLOAK_HOST=http://localhost:8080
export KEYCLOAK_CLIENT_ID=...
export KEYCLOAK_CLIENT_SECRET=...
```

### Running Translatr

Start database and Redis (Docker/docker-compose needed):

```
docker-compose up -d
```

Then, start the application:

```
bin/activator ~run -Dconfig.file=dev.conf
```

### Testing

Unit and integration tests live in the **test/** directory. In Eclipse, this
directory should be a source directory.

Unit tests can be run by issuing the following command:

```
bin/activator clean jacoco:cover -Dconfig.file=test.conf -J-Xmx1g
```

Jacoco generates a coverage report in **target/scala-2.11/jacoco/html/index.html**

### Debugging

For debugging with your favorite IDE, just add the parameters `-jvm-debug 9999`
to the bin/activator command. Then connect your IDE to localhost with port 9999.

```
bin/activator ~run -Dconfig.file=dev.conf -jvm-debug 9999
```

## Pull request guidelines

1. Pull requests should link to at least one issue that it is solving. The issue
itself should be descriptive so that a reviewer can understand what the PR is
doing. The description in the PR should only add any useful additional
information needed that is not in the issue.
1. Keep pull requests small and simple. A pull request should generally address
one issue. This makes it easier to review the pull request, and document the
changes.
1. Test your code before submitting a pull request! It is unsafe to merge
untested code even if you think it will work without testing. Creating unit
tests for any changes is a good idea, and is highly encouraged.
1. Before issuing a pull request, sync with the main repository and address any
conflicts that may exist.

**Working on your first Pull Request?** You can learn how from this *free*
series [How to Contribute to an Open Source Project on
GitHub](https://egghead.io/series/how-to-contribute-to-an-open-source-project-on-github)

### Style Guide

Use the [Google style guide for Java](https://raw.githubusercontent.com/google/styleguide/gh-pages/eclipse-java-google-style.xml).

