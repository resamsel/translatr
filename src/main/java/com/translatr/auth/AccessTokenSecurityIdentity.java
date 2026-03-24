package com.translatr.auth;

import com.translatr.model.User;
import io.quarkus.security.credential.Credential;
import io.quarkus.security.credential.TokenCredential;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;

import java.security.Permission;
import java.security.Principal;
import java.util.Map;
import java.util.Set;

public class AccessTokenSecurityIdentity implements SecurityIdentity {

    private final User  user;
    private final String token;

    public AccessTokenSecurityIdentity(User user, String token) {
        this.user  = user;
        this.token = token;
    }

    public User getUser() { return user; }

    @Override public Principal getPrincipal() {
        return () -> user.username;
    }

    @Override public boolean isAnonymous() { return false; }

    @Override public Set<String> getRoles() {
        return user.role != null ? Set.of(user.role.name()) : Set.of("User");
    }

    @Override public boolean hasRole(String role) { return getRoles().contains(role); }

    @Override public <T extends Credential> T getCredential(Class<T> credentialType) {
        if (credentialType == TokenCredential.class)
            return credentialType.cast(new TokenCredential(token, "access_token"));
        return null;
    }

    @Override public Set<Credential> getCredentials() {
        return Set.of(new TokenCredential(token, "access_token"));
    }

    @Override public <T> T getAttribute(String name) { return null; }

    @Override public Map<String, Object> getAttributes() { return Map.of(); }

    @Override
    public java.util.Set<java.security.Permission> getPermissions() {
        return java.util.Collections.emptySet();
    }

    @Override public Uni<Boolean> checkPermission(Permission permission) {
        return Uni.createFrom().item(true);
    }
}
