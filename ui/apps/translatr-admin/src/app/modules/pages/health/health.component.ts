import { ChangeDetectionStrategy, Component } from '@angular/core';
import { AuthClientService, OidcProviderStatus } from '@dev/translatr-sdk';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { catchError, map, shareReplay, startWith, switchMap } from 'rxjs/operators';

export type ProviderStatusKind = 'active' | 'listedNotUsable' | 'notListed';

type HealthProvidersVm =
  | { status: 'loading' }
  | { status: 'loaded'; providers: OidcProviderStatus[] }
  | { status: 'error' };

/**
 * Admin "Health" page. Currently surfaces the OIDC identity-provider diagnostics
 * from {@code GET /api/oidc-providers} (admin only); future health information can
 * be added as sibling sections.
 */
@Component({
  standalone: false,
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'dev-health',
  templateUrl: './health.component.html',
  styleUrls: ['./health.component.scss']
})
export class HealthComponent {
  private readonly refresh$ = new BehaviorSubject<void>(undefined);

  readonly vm$: Observable<HealthProvidersVm> = this.refresh$.pipe(
    switchMap(() =>
      this.authClientService.getProviderStatus().pipe(
        map((providers): HealthProvidersVm => ({ status: 'loaded', providers })),
        catchError((): Observable<HealthProvidersVm> => of({ status: 'error' })),
        startWith<HealthProvidersVm>({ status: 'loading' })
      )
    ),
    shareReplay({ bufferSize: 1, refCount: true })
  );

  constructor(private readonly authClientService: AuthClientService) {}

  refresh(): void {
    this.refresh$.next();
  }

  statusOf(provider: OidcProviderStatus): ProviderStatusKind {
    if (provider.active) {
      return 'active';
    }

    return provider.listed ? 'listedNotUsable' : 'notListed';
  }
}
