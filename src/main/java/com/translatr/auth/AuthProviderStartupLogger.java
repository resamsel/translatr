package com.translatr.auth;

import com.translatr.service.AuthProviderStatusService;
import com.translatr.service.OidcProviderStatus;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class AuthProviderStartupLogger {

    private static final Logger LOG = Logger.getLogger(AuthProviderStartupLogger.class);

    @Inject AuthProviderStatusService statusService;

    void onStart(@Observes StartupEvent ev) {
        try {
            List<OidcProviderStatus> all = statusService.evaluateAll();
            List<String> active = all.stream().filter(OidcProviderStatus::active)
                    .map(OidcProviderStatus::key).toList();
            List<String> broken = all.stream()
                    .filter(s -> s.listed() && !s.active())
                    .map(s -> s.key() + ": " + String.join(", ", s.errors()))
                    .toList();

            if (active.isEmpty()) {
                LOG.warn("No usable auth providers configured — the login page will be empty.");
            } else {
                LOG.infof("Active auth providers: %s", active);
            }
            if (!broken.isEmpty()) {
                LOG.warnf("Auth providers listed but not usable: %s",
                        broken.stream().collect(Collectors.joining("; ", "[", "]")));
            }
        } catch (RuntimeException e) {
            LOG.warnf("Auth provider status evaluation failed at startup: %s", e.getMessage());
        }
    }
}
