package com.translatr.dto;

import com.translatr.service.OidcProviderStatus;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Schema(name = "OidcProviderStatus",
        description = "Configuration and diagnostics for one auth provider (admin only).")
public class OidcProviderStatusDto {

    @Schema(readOnly = true, description = "Provider key, e.g. \"google\".")
    public String key;

    @Schema(readOnly = true, description = "Present in AUTH_PROVIDERS.")
    public boolean listed;

    @Schema(readOnly = true, description = "Listed AND fully configured AND no errors — usable now.")
    public boolean active;

    @Schema(readOnly = true, nullable = true, description = "Quarkus built-in preset name, or null (Keycloak).")
    public String provider;

    @Schema(readOnly = true, nullable = true, description = "OIDC issuer URL; returned verbatim (not a credential).")
    public String authServerUrl;

    @Schema(readOnly = true, nullable = true, description = "OAuth client id; returned verbatim (not a credential).")
    public String clientId;

    @Schema(readOnly = true, nullable = true,
            description = "Masked as \"***len:<N>***\" where <N> is the configured secret's length, "
                        + "or null when no secret is configured. The real secret is never returned.")
    public String clientSecret;

    @Schema(readOnly = true, required = true, description = "Effective scope list (possibly empty).")
    public List<String> scopes;

    @Schema(readOnly = true, required = true, description = "Configuration problems (empty when healthy).")
    public List<String> errors;

    public static OidcProviderStatusDto from(OidcProviderStatus s) {
        OidcProviderStatusDto d = new OidcProviderStatusDto();
        d.key           = s.key();
        d.listed        = s.listed();
        d.active        = s.active();
        d.provider      = s.provider();
        d.authServerUrl = s.authServerUrl();
        d.clientId      = s.clientId();
        d.clientSecret  = s.clientSecret();   // already masked by the service
        d.scopes        = s.scopes();
        d.errors        = s.errors();
        return d;
    }
}
