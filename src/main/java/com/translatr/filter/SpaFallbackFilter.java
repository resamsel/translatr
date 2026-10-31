package com.translatr.filter;

import io.quarkus.vertx.http.runtime.filters.Filters;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import java.util.List;

/**
 * Deep-link fallback for the two Angular SPAs Quinoa serves from one static root
 * ({@code /ui/**} and {@code /admin/**}).
 *
 * <p>Quinoa's own {@code enable-spa-routing} falls back to a single {@code /index.html}, which
 * cannot tell the two apps apart, so it is off. Instead, a {@code GET}/{@code HEAD} for a
 * file-less path below an SPA prefix (e.g. {@code /ui/projects/1}) is rerouted to that SPA's
 * {@code index.html}. Paths with a file extension (assets) are left to the static handler; the
 * rerouted {@code index.html} path has one, so it cannot loop.
 *
 * <p>Bare {@code /ui} is left alone on purpose: in non-prod profiles {@code UiLandingResource}
 * owns it.
 */
@ApplicationScoped
public class SpaFallbackFilter {

    private static final List<String> SPA_PREFIXES = List.of("/ui/", "/admin/");

    public void init(@Observes Filters filters) {
        filters.register(this::fallback, 90);
    }

    private void fallback(RoutingContext ctx) {
        HttpMethod method = ctx.request().method();
        if (method == HttpMethod.GET || method == HttpMethod.HEAD) {
            String path = ctx.normalizedPath();
            String prefix = spaPrefix(path);
            if (prefix != null && !hasExtension(path)) {
                ctx.reroute(prefix + "index.html");
                return;
            }
        }
        ctx.next();
    }

    private static String spaPrefix(String path) {
        if (path.equals("/admin")) {
            return "/admin/";
        }
        for (String prefix : SPA_PREFIXES) {
            if (path.startsWith(prefix)) {
                return prefix;
            }
        }
        return null;
    }

    private static boolean hasExtension(String path) {
        return path.substring(path.lastIndexOf('/') + 1).indexOf('.') >= 0;
    }
}
