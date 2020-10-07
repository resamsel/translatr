# Contributing

You want to help out on Translatr? Great, feel welcome and read the following
sections in order to know what and how to work on something.

1. [How to file a bug report](#how-to-file-a-bug-report)
1. [How to suggest a new feature](#how-to-suggest-a-new-feature)
1. [How to set up your environment and run tests](#how-to-set-up-your-environment-and-run-tests)
1. [How to add support for a new file format](#how-to-add-support-for-a-new-file-format)
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

### Prerequisites

For development, you need the following dependencies.

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

## How to add support for a new file format

Adding support for new file formats is quite easy. The few steps necessary are defined in the following sections.

### Add Enum Value

The `FileType` enum defines available file formats.

```java
public enum FileType
{
	JavaProperties("java_properties"),

	PlayMessages("play_messages"),

	Gettext("gettext"),

	Json("json");
}
```

### Create Importer

The importer takes a file from an InputStream and creates key/value pairs from that file. The importer should be placed
inside the `importers` package.

```java
public class JsonImporter extends AbstractImporter implements Importer {

  @Inject
  public JsonImporter(KeyService keyService, MessageService messageService) {
    super(keyService, messageService);
  }

  @Override
  Properties retrieveProperties(InputStream inputStream, Locale locale) throws Exception {
    JsonNode json = Json.mapper().readTree(inputStream);
    Properties properties = new Properties();

    if (json.isObject()) {
      ObjectNode jsonObject = (ObjectNode) json;

      stream(spliteratorUnknownSize(jsonObject.fields(), 0), false)
          .forEach(entry -> properties.put(entry.getKey(), entry.getValue().asText()));
    }

    return properties;
  }
}
```

### Create Exporter

The exporter gets a locale with messages and transforms them into a byte array. The exporter should be placed inside the
`exporters` package.

```java
public class JsonExporter extends AbstractExporter implements Exporter {
  protected static final ObjectMapper SORTED_MAPPER = new ObjectMapper()
          .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
          .configure(SerializationFeature.INDENT_OUTPUT, true);

  private final ObjectMapper mapper;

  public JsonExporter() {
    this(SORTED_MAPPER);
  }

  public JsonExporter(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public byte[] apply(Locale locale) {
    if (locale == null || locale.messages == null) {
      return new byte[]{};
    }

    Map<String, String> messages = locale.messages
            .stream()
            .collect(toMap(m -> m.key.name, m -> m.value));

    try {
      return mapper.writeValueAsBytes(messages);
    } catch (JsonProcessingException e) {
      return new byte[]{};
    }
  }

  @Override
  public String getFilename(Locale locale) {
    return locale.name + ".json";
  }
}
```

### Registering Importer/Exporter

The importer and exporter need to be registered in the `FileFormatRegistry` class. 

### Register in the UI

```typescript
export enum FileType {
  JavaProperties = 'java_properties',
  PlayMessages = 'play_messages',
  Gettext = 'gettext',
  Json = 'json'
}

export const fileTypes = [
  FileType.JavaProperties,
  FileType.PlayMessages,
  FileType.Gettext,
  FileType.Json
];

export const fileTypeNames = {
  [FileType.JavaProperties]: 'Java Properties',
  [FileType.PlayMessages]: 'Play Messages',
  [FileType.Gettext]: 'Gettext',
  [FileType.Json]: 'JSON'
};
```

### Testing

To be able to validate importing and exporting unit tests need to be added. See `JsonImporterTest` and
`JsonExporterTest` for examples.

### Create Pull Request

With the changes above, create a pull request. That PR should be handled quite easily and is added in the next version.
See [Pull request guidelines](#pull-request-guidelines) for details.

## Publishing Docker Image

```
$ bin/activator stage docker:publish
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

