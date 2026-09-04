import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { AuthClient } from '@dev/translatr-model';
import { Observable } from 'rxjs';
import { OidcProvidersService } from '../generated/api/oidcProviders.service';
import { Configuration } from '../generated/configuration';
import { OidcProviderStatus } from '../generated/model/oidcProviderStatus';

@Injectable({
  providedIn: 'root'
})
export class AuthClientService {
  // Constructed manually (not constructor-injected) because the generated
  // OidcProvidersService's BaseService falls back to the hardcoded absolute
  // basePath 'http://localhost' whenever no BASE_PATH/Configuration provider
  // is registered in the app's DI graph — which this app deliberately does
  // not do. Passing an explicit Configuration with basePath: '' keeps
  // requests relative (e.g. '/api/oidc-providers'), matching how the rest
  // of this app calls the backend from whatever origin it's actually served
  // from. The second positional constructor argument ('') below is the
  // generated class's own basePath parameter — it's non-optional in the
  // generated constructor signature, but its value is redundant here because
  // the explicit Configuration passed as the third argument takes precedence
  // over it at request time. Keep passing it; it's required-but-ignored, not
  // dead code to delete.
  private readonly oidcProvidersService: OidcProvidersService;

  constructor(private readonly http: HttpClient) {
    this.oidcProvidersService = new OidcProvidersService(http, '', new Configuration({ basePath: '' }));
  }

  find(): Observable<AuthClient[]> {
    return this.http.get<AuthClient[]>('/api/authclients');
  }

  getProviderStatus(): Observable<OidcProviderStatus[]> {
    return this.oidcProvidersService.listOidcProviders();
  }
}
