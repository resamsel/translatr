package com.translatr.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Access-token scopes a resource method needs; all listed values are required. A method-level
 * annotation fully replaces a class-level one. Enforced by {@link ScopeEnforcementFilter} for
 * access tokens only — session and anonymous callers are unaffected.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface RequiresScope {
    Scope[] value();
}
