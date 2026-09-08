package com.translatr.auth;

import com.translatr.model.AccessToken;
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

    private final User   user;
    private final String token;
    private final Long   keyId;

    public AccessTokenSecurityIdentity(AccessToken accessToken) {
        this.user  = accessToken.user;
        this.token = accessToken.key;
        this.keyId = accessToken.id;
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

    /** Key under which the resolved {@link User} is exposed via {@link #getAttribute}. */
    public static final String USER_ATTRIBUTE = "user";
    /** Key under which the {@link AccessToken} id (as String) is exposed for observability. */
    public static final String KEY_ID_ATTRIBUTE = "translatr.key_id";

    @SuppressWarnings("unchecked")
    @Override public <T> T getAttribute(String name) {
        if (USER_ATTRIBUTE.equals(name))   return (T) user;
        if (KEY_ID_ATTRIBUTE.equals(name)) return keyId != null ? (T) String.valueOf(keyId) : null;
        return null;
    }

    @Override public Map<String, Object> getAttributes() {
        return keyId != null
                ? Map.of(USER_ATTRIBUTE, user, KEY_ID_ATTRIBUTE, String.valueOf(keyId))
                : Map.of(USER_ATTRIBUTE, user);
    }

    @Override
    public Set<Permission> getPermissions() {
        return java.util.Collections.emptySet();
    }

    @Override public Uni<Boolean> checkPermission(Permission permission) {
        return Uni.createFrom().item(true);
    }
}
