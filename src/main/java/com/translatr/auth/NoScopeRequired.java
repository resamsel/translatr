package com.translatr.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Explicit opt-out from scope checks for endpoints where a scope is meaningless (health, login,
 * UI redirects, …). Without this or {@link RequiresScope}, access tokens are denied.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface NoScopeRequired {
}
