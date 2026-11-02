package com.translatr.auth;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.Reader;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Keeps {@code openapi.yaml}'s documented access-token scopes ({@code x-required-scopes}, the
 * description line, {@code x-access-token: denied}) in lock-step with the {@link RequiresScope} /
 * {@link NoScopeRequired} annotations the filter actually enforces — drift either way fails.
 */
class OpenApiScopeConsistencyTest {

    private static final Set<String> HTTP_VERBS = Set.of("get", "post", "put", "patch", "delete");

    @SuppressWarnings("unchecked")
    private static Map<String, Object> spec() throws Exception {
        try (Reader r = Files.newBufferedReader(Path.of("src/main/resources/META-INF/openapi.yaml"))) {
            return new Yaml().load(r);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void everyOperationDocumentsTheScopesItEnforces() throws Exception {
        List<String> problems = new ArrayList<>();
        Map<String, Object> paths = (Map<String, Object>) spec().get("paths");
        for (var path : paths.entrySet()) {
            for (var op : ((Map<String, Object>) path.getValue()).entrySet()) {
                if (!HTTP_VERBS.contains(op.getKey())) continue;
                Map<String, Object> operation = (Map<String, Object>) op.getValue();
                String operationId = (String) operation.get("operationId");
                String description = String.valueOf(operation.get("description"));

                List<Map.Entry<Class<?>, Method>> matches = new ArrayList<>();
                for (Class<?> cls : ScopeAnnotationCompletenessTest.resourceClasses())
                    ScopeAnnotationCompletenessTest.jaxRsMethods(cls)
                            .filter(m -> m.getName().equals(operationId))
                            .forEach(m -> matches.add(Map.entry(cls, m)));
                if (matches.size() != 1) {
                    problems.add(operationId + ": expected exactly one resource method, found " + matches.size());
                    continue;
                }
                Class<?> cls = matches.get(0).getKey();
                Method method = matches.get(0).getValue();

                if (ScopeAnnotationCompletenessTest.TOKEN_DENIED.contains(cls)) {
                    if (!"denied".equals(operation.get("x-access-token")))
                        problems.add(operationId + ": missing x-access-token: denied");
                    if (operation.containsKey("x-required-scopes"))
                        problems.add(operationId + ": token-denied operation must not declare x-required-scopes");
                    if (!description.contains("Not available to access tokens"))
                        problems.add(operationId + ": description lacks 'Not available to access tokens'");
                    continue;
                }

                Set<String> enforced = ScopeEnforcementFilter.requiredScopes(method, cls).orElseThrow()
                        .stream().map(Scope::value).collect(Collectors.toCollection(TreeSet::new));
                Object documented = operation.get("x-required-scopes");
                if (!(documented instanceof List<?> list)) {
                    problems.add(operationId + ": missing x-required-scopes");
                } else if (!new TreeSet<>((List<String>) list).equals(enforced)) {
                    problems.add(operationId + ": x-required-scopes " + list + " != enforced " + enforced);
                }
                String line = "Required access-token scopes: "
                        + (enforced.isEmpty() ? "none" : String.join(", ", enforced));
                if (!description.contains(line))
                    problems.add(operationId + ": description lacks '" + line + "'");
            }
        }
        assertThat(problems).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void declaresAccessTokenSecuritySchemes() throws Exception {
        Map<String, Object> spec = spec();
        Map<String, Object> schemes = (Map<String, Object>)
                ((Map<String, Object>) spec.get("components")).get("securitySchemes");
        assertThat(schemes).containsEntry("accessTokenHeader",
                Map.of("type", "apiKey", "in", "header", "name", "X-Access-Token"));
        assertThat(schemes).containsEntry("accessTokenQuery",
                Map.of("type", "apiKey", "in", "query", "name", "access_token"));
        assertThat(schemes).containsEntry("bearerToken", Map.of("type", "http", "scheme", "bearer"));
        assertThat((List<Map<String, Object>>) spec.get("security")).containsExactlyInAnyOrder(
                Map.of("accessTokenHeader", List.of()),
                Map.of("accessTokenQuery", List.of()),
                Map.of("bearerToken", List.of()),
                Map.of());
    }

    @Test
    @SuppressWarnings("unchecked")
    void fetchParamDocumentsMemberReadRequirement() throws Exception {
        Map<String, Object> fetch = (Map<String, Object>) ((Map<String, Object>)
                ((Map<String, Object>) spec().get("components")).get("parameters")).get("FetchParam");
        assertThat(String.valueOf(fetch.get("description"))).contains("`read:member`");
    }
}
