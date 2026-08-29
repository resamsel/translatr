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

The backend is a **Quarkus** application (Java 21, Gradle). The frontend is an
**Angular 22** single-page app (Nx 23 workspace, Cypress 15 e2e) served by the
[Quinoa](https://quarkiverse.github.io/quarkiverse-docs/quarkus-quinoa/dev/)
extension. Authentication is handled by **Keycloak**.

### Prerequisites

| Tool | Version | Notes |
|------|---------|-------|
| Java JDK | 21 | Temurin / Corretto both work |
| Docker + Docker Compose | any recent | runs PostgreSQL + Keycloak |
| Node.js | 22.12 (see `ui/.nvmrc`) | only needed for UI hot-reload; not required for the default dev flow |

### Running it (standard — no Node required)

The default workflow serves the pre-built Angular artefacts from `public/ui`
directly via Quinoa. No Angular dev server or local Node installation is needed.

1. **Start the backing services** (PostgreSQL + Keycloak):

   ```bash
   docker compose up -d
   ```

2. **Copy your credentials into `.env`** (create it if it doesn't exist yet):

   ```
   AUTH_PROVIDERS=keycloak
   KEYCLOAK_CLIENT_ID=translatr-localhost
   KEYCLOAK_CLIENT_SECRET=<your-secret>
   ```

3. **Start Quarkus in dev mode** — the `copyUiToBuild` task runs automatically
   first, staging the pre-built UI into `build/quinoa/`:

   ```bash
   export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home
   ./gradlew quarkusDev
   ```

4. Open [http://localhost:9000](http://localhost:9000).

### Running it (UI hot-reload — requires Node 22.12)

Use this when you are actively working on the Angular frontend and want instant
browser refresh on file save.

1. Complete steps 1–2 from the standard flow above.

2. Install UI dependencies (first time only):

   ```bash
   cd ui && npm install
   ```

3. **Terminal A** — start the Angular dev server:

   ```bash
   cd ui && npm start          # listens on port 4210
   ```

4. **Terminal B** — uncomment the two `dev-server` lines in
   `src/main/resources/application-dev.properties`:

   ```properties
   %dev.quarkus.quinoa.dev-server.port=4210
   %dev.quarkus.quinoa.dev-server.managed=false
   ```

   Then start Quarkus:

   ```bash
   ./gradlew quarkusDev
   ```

5. Open [http://localhost:9000](http://localhost:9000).

> **Note on Node versions**
> Use the Node version recorded in `ui/.nvmrc` (currently **22.12**) for
> `npm start` / `npm run build`.

### Authorisation

At least one auth provider must be configured. The recommended choice for local
development is Keycloak (started automatically by `docker-compose up`).

#### Keycloak (recommended)

The `docker-compose.yml` starts a Keycloak instance on port **8088** and
imports the `docker/Translatr-realm.json` realm automatically.

```
export AUTH_PROVIDERS=keycloak
export KEYCLOAK_CLIENT_ID=translatr-localhost
export KEYCLOAK_CLIENT_SECRET=<client-secret-from-realm>
```

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

### Testing

Unit and integration tests use **JUnit 5** and **RestAssured** and run against
the live PostgreSQL instance started by `docker-compose`.

```bash
./gradlew test
```

The test report is written to `build/reports/tests/test/index.html`.

### Debugging

Add the Quarkus debug flags to attach a remote debugger on port 5005:

```bash
./gradlew quarkusDev -Dsuspend=false -Ddebug=5005
```

Then connect your IDE to `localhost:5005`.

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
$ npm run build:docker && npm run publish:docker
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
