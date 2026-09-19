import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthClientService, OidcProviderStatus } from '@dev/translatr-sdk';
import { TranslocoModule } from '@jsverse/transloco';
import { Observable, of } from 'rxjs';
import { catchError, map, shareReplay, startWith } from 'rxjs/operators';
import { AdminPageComponent } from '../../admin-page/admin-page.component';

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
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'dev-health',
  templateUrl: './health.component.html',
  styleUrls: ['./health.component.scss'],
  imports: [
    CommonModule,
    TranslocoModule,
    AdminPageComponent,
    MatIconModule,
    MatExpansionModule,
    MatProgressSpinnerModule,
    MatCardModule,
    MatChipsModule
  ]
})
export class HealthComponent {
  readonly vm$: Observable<HealthProvidersVm> = this.authClientService.getProviderStatus().pipe(
    map((providers): HealthProvidersVm => ({ status: 'loaded', providers })),
    catchError((): Observable<HealthProvidersVm> => of({ status: 'error' })),
    startWith<HealthProvidersVm>({ status: 'loading' }),
    shareReplay({ bufferSize: 1, refCount: true }),
  );

  readonly healthy$: Observable<boolean> = this.vm$.pipe(
    map((v) => v.status === 'loaded' && v.providers.every(p => !p.listed || p.errors.length === 0)),
  );

  constructor(private readonly authClientService: AuthClientService) {}

  statusOf(provider: OidcProviderStatus): ProviderStatusKind {
    if (provider.active) {
      return 'active';
    }

    return provider.listed ? 'listedNotUsable' : 'notListed';
  }
}
