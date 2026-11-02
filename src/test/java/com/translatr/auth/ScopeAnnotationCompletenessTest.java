package com.translatr.auth;

import com.translatr.controller.ActivityResource;
import com.translatr.controller.GlobalFeatureFlagResource;
import com.translatr.controller.HealthResource;
import com.translatr.controller.LocaleTransferResource;
import com.translatr.controller.OidcProviderResource;
import com.translatr.controller.ProjectResource;
import com.translatr.controller.UserResource;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Every JAX-RS resource method must declare {@link RequiresScope} or {@link NoScopeRequired};
 * otherwise {@link ScopeEnforcementFilter} silently denies access tokens. Classes in
 * {@link #TOKEN_DENIED} are deliberately left unannotated.
 */
class ScopeAnnotationCompletenessTest {

    /** Resources access tokens may never call (admin config with no matching scope). */
    static final Set<Class<?>> TOKEN_DENIED = Set.of(OidcProviderResource.class);

    private static final List<Class<? extends Annotation>> HTTP_METHODS =
            List.of(GET.class, POST.class, PUT.class, PATCH.class, DELETE.class);

    @Test
    void everyResourceMethodDeclaresItsScopes() throws Exception {
        List<String> missing = new ArrayList<>();
        for (Class<?> cls : resourceClasses()) {
            if (TOKEN_DENIED.contains(cls)) continue;
            jaxRsMethods(cls)
                    .filter(m -> ScopeEnforcementFilter.requiredScopes(m, cls).isEmpty())
                    .forEach(m -> missing.add(cls.getSimpleName() + "#" + m.getName()));
        }
        assertThat(missing).as("resource methods without @RequiresScope/@NoScopeRequired").isEmpty();
    }

    @Test
    void tokenDeniedResourcesStayUnannotated() throws Exception {
        for (Class<?> cls : TOKEN_DENIED)
            jaxRsMethods(cls).forEach(m ->
                    assertThat(ScopeEnforcementFilter.requiredScopes(m, cls)).as(m.getName()).isEmpty());
    }

    @Test
    void mappingMatchesSpecTable() throws Exception {
        assertThat(required(ProjectResource.class, "createProject")).containsExactly(Scope.ProjectWrite);
        assertThat(required(ProjectResource.class, "findProjects")).containsExactly(Scope.ProjectRead);
        assertThat(required(ActivityResource.class, "findActivities")).containsExactly(Scope.ProjectRead);
        assertThat(required(UserResource.class, "getCurrentUser")).containsExactly(Scope.UserRead);
        assertThat(required(UserResource.class, "saveUserSettings")).containsExactly(Scope.UserWrite);
        assertThat(required(GlobalFeatureFlagResource.class, "setGlobalFeatureFlag")).containsExactly(Scope.FeatureFlagWrite);
        assertThat(required(LocaleTransferResource.class, "upload"))
                .containsExactlyInAnyOrder(Scope.LocaleWrite, Scope.MessageWrite);
        assertThat(required(LocaleTransferResource.class, "download"))
                .containsExactlyInAnyOrder(Scope.LocaleRead, Scope.MessageRead);
        assertThat(required(HealthResource.class, "getHealth")).isEmpty();
    }

    private static Set<Scope> required(Class<?> cls, String method) throws Exception {
        Method m = jaxRsMethods(cls).filter(x -> x.getName().equals(method)).findFirst().orElseThrow();
        return ScopeEnforcementFilter.requiredScopes(m, cls).orElseThrow();
    }

    /** All {@code com.translatr.controller.*Resource} classes. */
    static List<Class<?>> resourceClasses() throws IOException, ClassNotFoundException {
        List<Class<?>> result = new ArrayList<>();
        try (Stream<Path> files = Files.list(Path.of("src/main/java/com/translatr/controller"))) {
            for (Path p : files.filter(f -> f.getFileName().toString().endsWith("Resource.java")).toList()) {
                String name = p.getFileName().toString().replace(".java", "");
                result.add(Class.forName("com.translatr.controller." + name));
            }
        }
        assertThat(result).isNotEmpty();
        return result;
    }

    /**
     * Public methods of {@code resource} that carry an HTTP-method annotation themselves or on the
     * same-signature method of an implemented (generated) interface.
     */
    static Stream<Method> jaxRsMethods(Class<?> resource) {
        return Stream.of(resource.getDeclaredMethods())
                .filter(m -> java.lang.reflect.Modifier.isPublic(m.getModifiers()) && !m.isSynthetic())
                .filter(m -> isHttp(m) || Stream.of(resource.getInterfaces()).anyMatch(i -> {
                    try {
                        return isHttp(i.getMethod(m.getName(), m.getParameterTypes()));
                    } catch (NoSuchMethodException e) {
                        return false;
                    }
                }));
    }

    private static boolean isHttp(Method m) {
        return HTTP_METHODS.stream().anyMatch(m::isAnnotationPresent);
    }
}
