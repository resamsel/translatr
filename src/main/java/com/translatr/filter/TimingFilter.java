package com.translatr.filter;

import io.quarkus.vertx.http.runtime.filters.Filters;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class TimingFilter {

    private static final Logger LOG = LoggerFactory.getLogger(TimingFilter.class);

    public void init(@Observes Filters filters) {
        filters.register(this::time, 100);
    }

    private void time(RoutingContext ctx) {
        long start = System.currentTimeMillis();
        ctx.addBodyEndHandler(v ->
            LOG.debug("{} {} {}ms",
                ctx.request().method(), ctx.request().path(),
                System.currentTimeMillis() - start));
        ctx.next();
    }
}
