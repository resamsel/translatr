package com.translatr.auth;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ScopeEnforcementFilterTest {

    @RequiresScope(Scope.ProjectRead)
    static class Fixture {
        public void read() {}
        @RequiresScope(Scope.ProjectWrite) public void write() {}
        @RequiresScope({Scope.LocaleWrite, Scope.MessageWrite}) public void importLocale() {}
        @NoScopeRequired public void open() {}
    }

    static class Unannotated {
        public void get() {}
    }

    interface Api { void read(); }

    static class ApiImpl implements Api {
        @RequiresScope(Scope.KeyRead) @Override public void read() {}
    }

    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();

    @AfterEach
    void clearFlag() {
        System.clearProperty("translatr.observability.metrics-enabled");
    }

    @Test
    void sessionIdentity_passes() throws Exception {
        SecurityIdentity session = mock(SecurityIdentity.class);
        when(session.isAnonymous()).thenReturn(false);
        ContainerRequestContext ctx = run(session, Fixture.class, "write", null);
        verify(ctx, never()).abortWith(any());
    }

    @Test
    void anonymous_passes() throws Exception {
        SecurityIdentity anon = mock(SecurityIdentity.class);
        when(anon.isAnonymous()).thenReturn(true);
        ContainerRequestContext ctx = run(anon, Unannotated.class, "get", null);
        verify(ctx, never()).abortWith(any());
    }

    @Test
    void tokenWithScope_passes() throws Exception {
        ContainerRequestContext ctx = run(token(Scope.ProjectRead), Fixture.class, "read", null);
        verify(ctx, never()).abortWith(any());
    }

    @Test
    void tokenMissingScope_403() throws Exception {
        Response r = aborted(run(token(Scope.ProjectRead), Fixture.class, "write", null));
        assertThat(r.getStatus()).isEqualTo(403);
        assertThat(((InsufficientScopeResponse) r.getEntity()).error).isEqualTo("insufficient_scope");
        assertThat(((InsufficientScopeResponse) r.getEntity()).required).containsExactly("write:project");
        assertThat(r.getHeaderString(HttpHeaders.WWW_AUTHENTICATE))
                .isEqualTo("Bearer error=\"insufficient_scope\", scope=\"write:project\"");
    }

    @Test
    void multipleRequired_needsAll() throws Exception {
        Response r = aborted(run(token(Scope.LocaleWrite), Fixture.class, "importLocale", null));
        assertThat(((InsufficientScopeResponse) r.getEntity()).required).containsExactly("write:message");
    }

    @Test
    void missingScopesAreSortedAndSpaceSeparatedInHeader() throws Exception {
        Response r = aborted(run(token(), Fixture.class, "importLocale", null));
        assertThat(((InsufficientScopeResponse) r.getEntity()).required)
                .containsExactly("write:locale", "write:message");
        assertThat(r.getHeaderString(HttpHeaders.WWW_AUTHENTICATE))
                .isEqualTo("Bearer error=\"insufficient_scope\", scope=\"write:locale write:message\"");
    }

    @Test
    void methodOverridesClass() throws Exception {
        assertThat(aborted(run(token(Scope.ProjectRead), Fixture.class, "write", null)).getStatus()).isEqualTo(403);
        assertThat(ScopeEnforcementFilter.requiredScopes(Fixture.class.getMethod("write"), Fixture.class))
                .contains(EnumSet.of(Scope.ProjectWrite));
    }

    @Test
    void noScopeRequired_passes() throws Exception {
        ContainerRequestContext ctx = run(token(), Fixture.class, "open", null);
        verify(ctx, never()).abortWith(any());
    }

    @Test
    void unannotated_deniesToken() throws Exception {
        Response r = aborted(run(token(Scope.values()), Unannotated.class, "get", null));
        assertThat(r.getStatus()).isEqualTo(403);
        assertThat(((InsufficientScopeResponse) r.getEntity()).required).isEmpty();
        assertThat(ScopeEnforcementFilter.requiredScopes(Unannotated.class.getMethod("get"), Unannotated.class)).isEmpty();
    }

    @Test
    void interfaceMethodResolvesToImplAnnotation() throws Exception {
        assertThat(ScopeEnforcementFilter.requiredScopes(Api.class.getMethod("read"), ApiImpl.class))
                .contains(EnumSet.of(Scope.KeyRead));
    }

    @Test
    void fetchMembers_requiresMemberRead() throws Exception {
        Response r = aborted(run(token(Scope.ProjectRead), Fixture.class, "read", "progress, members"));
        assertThat(((InsufficientScopeResponse) r.getEntity()).required).containsExactly("read:member");
    }

    @Test
    void fetchMembersWithMemberRead_passes() throws Exception {
        ContainerRequestContext ctx = run(token(Scope.ProjectRead, Scope.MemberRead), Fixture.class, "read", "members");
        verify(ctx, never()).abortWith(any());
    }

    @Test
    void fetchMembersUppercase_matchesQuerySupportSemantics() throws Exception {
        // QuerySupport.wants is exact-match, so the services don't expand MEMBERS either.
        ContainerRequestContext ctx = run(token(Scope.ProjectRead), Fixture.class, "read", "MEMBERS");
        verify(ctx, never()).abortWith(any());
    }

    @Test
    void scopeDenial_incrementsMetricWhenEnabled() throws Exception {
        System.setProperty("translatr.observability.metrics-enabled", "true");
        run(token(), Fixture.class, "write", null);
        assertThat(registry.counter("translatr.apikey.auth.failures", "reason", "scope").count()).isEqualTo(1d);
    }

    @Test
    void scopeDenial_noMetricWhenDisabled() throws Exception {
        System.setProperty("translatr.observability.metrics-enabled", "false");
        run(token(), Fixture.class, "write", null);
        assertThat(registry.find("translatr.apikey.auth.failures").counter()).isNull();
    }

    private static SecurityIdentity token(Scope... scopes) {
        SecurityIdentity id = mock(SecurityIdentity.class);
        Set<Scope> set = scopes.length == 0 ? EnumSet.noneOf(Scope.class) : EnumSet.of(scopes[0], scopes);
        when(id.getAttribute(AccessTokenSecurityIdentity.SCOPES_ATTRIBUTE)).thenReturn(set);
        return id;
    }

    private ContainerRequestContext run(SecurityIdentity identity, Class<?> cls, String method, String fetch)
            throws Exception {
        Method m = cls.getMethod(method);
        ResourceInfo info = mock(ResourceInfo.class);
        doReturn(cls).when(info).getResourceClass();
        when(info.getResourceMethod()).thenReturn(m);
        UriInfo uri = mock(UriInfo.class);
        MultivaluedHashMap<String, String> query = new MultivaluedHashMap<>();
        if (fetch != null) query.putSingle("fetch", fetch);
        when(uri.getQueryParameters()).thenReturn(query);
        ContainerRequestContext ctx = mock(ContainerRequestContext.class);

        ScopeEnforcementFilter filter = new ScopeEnforcementFilter(registry, identity);
        filter.resourceInfo = info;
        filter.uriInfo = uri;
        filter.filter(ctx);
        return ctx;
    }

    private static Response aborted(ContainerRequestContext ctx) {
        ArgumentCaptor<Response> captor = ArgumentCaptor.forClass(Response.class);
        verify(ctx).abortWith(captor.capture());
        return captor.getValue();
    }
}
