export interface OidcProviderStatus {
  key: string;
  listed: boolean;
  active: boolean;
  provider: string | null;
  authServerUrl: string | null;
  clientId: string | null;
  /** Masked as "***len:<N>***", or null when no secret is configured. */
  clientSecret: string | null;
  scopes: string[];
  errors: string[];
}
