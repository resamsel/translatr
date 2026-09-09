import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { By } from '@angular/platform-browser';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { AuthClientService, OidcProviderStatus } from '@dev/translatr-sdk';
import { TranslocoTestingModule } from '@jsverse/transloco';
import { Subject, of, throwError } from 'rxjs';
import { HealthComponent } from './health.component';

const provider = (over: Partial<OidcProviderStatus>): OidcProviderStatus => ({
  key: 'keycloak',
  listed: true,
  active: true,
  provider: null,
  authServerUrl: 'http://localhost:8080/realms/Translatr',
  clientId: 'translatr-localhost',
  clientSecret: '***len:36***',
  scopes: ['openid'],
  errors: [],
  ...over
});

describe('HealthComponent', () => {
  let fixture: ComponentFixture<HealthComponent>;
  let component: HealthComponent;
  let authClientService: { getProviderStatus: jest.Mock };

  const setup = () => {
    fixture = TestBed.createComponent(HealthComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  };

  const cards = () => fixture.debugElement.queryAll(By.css('.provider-card'));

  beforeEach(
    waitForAsync(() => {
      authClientService = { getProviderStatus: jest.fn().mockReturnValue(of([])) };
      TestBed.configureTestingModule({
        declarations: [HealthComponent],
        imports: [
          NoopAnimationsModule,
          MatCardModule,
          MatChipsModule,
          MatProgressSpinnerModule,
          TranslocoTestingModule.forRoot({ langs: {}, translocoConfig: { availableLangs: ['en'] } })
        ],
        providers: [{ provide: AuthClientService, useValue: authClientService }]
      }).compileComponents();
    })
  );

  it('renders one card per provider', () => {
    authClientService.getProviderStatus.mockReturnValue(
      of([provider({ key: 'keycloak' }), provider({ key: 'google', provider: 'google' })])
    );
    setup();

    expect(cards()).toHaveLength(2);
    expect(cards()[0].nativeElement.textContent).toContain('keycloak');
    expect(cards()[1].nativeElement.textContent).toContain('google');
  });

  it('maps status: active / listed-not-usable / not-listed', () => {
    setup();
    expect(component.statusOf(provider({ active: true, listed: true }))).toBe('active');
    expect(component.statusOf(provider({ active: false, listed: true }))).toBe('listedNotUsable');
    expect(component.statusOf(provider({ active: false, listed: false }))).toBe('notListed');
  });

  it('reflects the status kind on the card class', () => {
    authClientService.getProviderStatus.mockReturnValue(
      of([provider({ key: 'github', active: false, listed: true, errors: ['client-secret is missing'] })])
    );
    setup();

    expect(cards()[0].nativeElement.classList).toContain('status-listedNotUsable');
  });

  it('renders each provider error as a list item', () => {
    authClientService.getProviderStatus.mockReturnValue(
      of([provider({ active: false, errors: ['client-id is missing', 'client-secret is missing'] })])
    );
    setup();

    const items = fixture.debugElement.queryAll(By.css('.provider-card .warnings li'));
    expect(items.map(i => i.nativeElement.textContent.trim())).toEqual([
      'client-id is missing',
      'client-secret is missing'
    ]);
  });

  it('lists errors of an active provider under .errors, of an inactive one under .warnings', () => {
    authClientService.getProviderStatus.mockReturnValue(
      of([
        provider({ key: 'active-with-errors', active: true, errors: ['token endpoint slow'] }),
        provider({ key: 'inactive-with-errors', active: false, errors: ['client-secret is missing'] })
      ])
    );
    setup();

    expect(cards()[0].query(By.css('.errors li')).nativeElement.textContent.trim()).toBe(
      'token endpoint slow'
    );
    expect(cards()[0].query(By.css('.warnings'))).toBeNull();
    expect(cards()[1].query(By.css('.warnings li')).nativeElement.textContent.trim()).toBe(
      'client-secret is missing'
    );
    expect(cards()[1].query(By.css('.errors'))).toBeNull();
  });

  it('shows a spinner while the request is in flight', () => {
    authClientService.getProviderStatus.mockReturnValue(new Subject<OidcProviderStatus[]>());
    setup();

    expect(fixture.debugElement.query(By.css('mat-progress-spinner'))).toBeTruthy();
    expect(cards()).toHaveLength(0);
  });

  it('shows an error message when the request fails', () => {
    authClientService.getProviderStatus.mockReturnValue(throwError(() => new Error('boom')));
    setup();

    expect(fixture.debugElement.query(By.css('.error'))).toBeTruthy();
    expect(cards()).toHaveLength(0);
  });
});
