/**
 * Development proxy configuration.
 *
 * Forwards /api/* to the Quarkus backend (port 9000).
 *
 * When the user is not authenticated, Quarkus OIDC (hybrid mode) responds with
 * a 302 redirect to Keycloak.  Angular's HttpClient (XHR) cannot follow a
 * cross-origin redirect, so the request fails with status 0.  The NgRx effect
 * dispatches `meLoadError`, the reducer sets `me` to `null`, and the
 * AuthGuard then redirects the browser to the Angular /login page, which in
 * turn navigates the browser (window.location) to the Quarkus /login/{provider}
 * endpoint – triggering the full browser-level OIDC redirect to Keycloak and
 * back.  No proxy-level manipulation is required.
 */
module.exports = {
  '/api': {
    target: 'http://localhost:9000',
    secure: false,
    logLevel: 'debug'
  }
};

