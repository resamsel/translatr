# Contributing

You want to help out on Translatr? Great, feel welcome and read the following
sections in order to know what and how to work on something.

1. [How to file a bug report](#how-to-file-a-bug-report)
1. [How to suggest a new feature](#how-to-suggest-a-new-feature)
1. [How to set up your environment and run tests](#how-to-set-up-your-environment-and-run-tests)
1. [How to add support for a new file format](#how-to-add-support-for-a-new-file-format)
1. [Contract-first OpenAPI (in progress)](#contract-first-openapi-in-progress)
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
| Node.js | 24 (see `ui/.nvmrc`) | only needed for UI hot-reload; not required for the default dev flow |

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
   OIDC_KEYCLOAK_AUTH_SERVER_URL=http://localhost:8080/realms/Translatr
   OIDC_KEYCLOAK_CLIENT_ID=translatr-localhost
   OIDC_KEYCLOAK_CLIENT_SECRET=<your-secret>
   ```

3. **Start Quarkus in dev mode** — the `copyUiToBuild` task runs automatically
   first, staging the pre-built UI into `build/quinoa/`:

   ```bash
   export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home
   ./gradlew quarkusDev
   ```

4. Open [http://localhost:9000](http://localhost:9000).

### Running it (UI hot-reload — requires Node 24)

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
> Use the Node version recorded in `ui/.nvmrc` (currently **24**) for
> `npm start` / `npm run build`.

### Authorisation

At least one auth provider must be configured. The recommended choice for local
development is Keycloak (started automatically by `docker-compose up`).

> **Logout.** `GET /logout` (`LogoutResource`) is a *local* logout: it expires the
> Quarkus OIDC cookies the browser sent (`q_session*`, `q_auth*`, `q_post_logout*`)
> and redirects to `<redirect-base>/ui`. It deliberately does **not** use Quarkus'
> RP-initiated logout, which redirects to the provider's `end_session_endpoint` —
> Google, GitHub, Facebook, Apple and X publish none, so it would fail for most of
> the roster. You stay signed in *at the identity provider*, so the next
> `/login/{provider}` may complete without another password prompt. Signing out at
> the provider is tracked in
> [#258](https://github.com/resamsel/translatr/issues/258).

#### Keycloak (recommended)

The `docker-compose.yml` starts a Keycloak instance on port **8080** and
imports the `docker/Translatr-realm.json` realm automatically.

```
export AUTH_PROVIDERS=keycloak
export OIDC_KEYCLOAK_AUTH_SERVER_URL=http://localhost:8080/realms/Translatr
export OIDC_KEYCLOAK_CLIENT_ID=translatr-localhost
export OIDC_KEYCLOAK_CLIENT_SECRET=<client-secret-from-realm>
```

#### Google

Credentials can be retrieved from the [Google Cloud Resource Manager page](https://code.google.com/apis/console).

```
export AUTH_PROVIDERS=google
export OIDC_GOOGLE_CLIENT_ID=...
export OIDC_GOOGLE_CLIENT_SECRET=...
```

#### GitHub

Credentials can be retrieved from the [Register a new OAuth application page](https://github.com/settings/applications/new).

```
export AUTH_PROVIDERS=github
export OIDC_GITHUB_CLIENT_ID=...
export OIDC_GITHUB_CLIENT_SECRET=...
```

#### Any other provider

`facebook`, `twitter`, `microsoft`, `apple` work the same way —
`OIDC_<PROVIDER>_CLIENT_ID` / `OIDC_<PROVIDER>_CLIENT_SECRET` and add `<provider>`
to `AUTH_PROVIDERS`. A provider not in the built-in roster can be added with
`TRANSLATR_AUTH_OIDC_<NAME>_PROVIDER` (a Quarkus preset such as `spotify`) plus
the same id/secret vars. Register the redirect URI `<backend-origin>/authenticate`
with the provider. List several at once: `AUTH_PROVIDERS=keycloak,google,github`.
A listed provider with no credentials is skipped at startup (logged), never a
boot failure. `GET /api/oidc-providers` (admin) shows each provider's status and
any configuration errors.

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

## Contract-first OpenAPI (in progress)

`src/main/resources/META-INF/openapi.yaml` is the source of truth for **migrated**
resources only. Everything else is still discovered by MicroProfile OpenAPI scanning
annotated JAX-RS resource classes; smallrye merges both into the document served at
runtime.

To migrate a resource to contract-first, in order:

1. Add or extend that resource's paths and schemas in `openapi.yaml`.
2. If you introduce a new schema, add its name to `build.gradle.kts`'s
   `openApiGenerate.globalProperties["models"]` comma-separated list. A schema left
   off that list is silently not generated — it surfaces later as a compile error,
   not as an obvious spec problem.
3. Make the Java resource class `implement` the generated interface instead of
   hand-declaring JAX-RS annotations (`@GET`, `@Path`, etc.).
4. Add the resource class to `mp.openapi.scan.exclude.classes` in
   `application.properties` so MicroProfile OpenAPI stops also scanning it.
5. On the frontend, repoint the hand-written Angular service to the generated
   model/client under `ui/libs/translatr-sdk/src/lib/generated/`.

**Base-path gotcha:** an Angular component or service that constructor-injects a
generated `*Service` directly gets a broken absolute `http://localhost` base URL —
the generated `BaseService`'s hardcoded fallback — unless the app registers a root
`BASE_PATH`/`Configuration` provider, which neither Angular app currently does. Until
someone adds that root-level provider, construct the service manually with
`new Configuration({ basePath: '' })` instead, as
`ui/libs/translatr-sdk/src/lib/services/auth-client.service.ts` does; every new
generated-service consumer must replicate this workaround for now.

See `docs/superpowers/specs/2026-09-04-contract-first-openapi-design.md` for the
full design rationale.

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
