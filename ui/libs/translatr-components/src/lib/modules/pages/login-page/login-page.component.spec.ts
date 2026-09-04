import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { RouterTestingModule } from '@angular/router/testing';
import { FontAwesomeTestingModule } from '@fortawesome/angular-fontawesome/testing';
import { TranslocoTestingModule } from '@jsverse/transloco';
import { NavbarTestingModule } from '@translatr/components/testing';
import { ENDPOINT_URL } from '@translatr/utils';
import { mockObservable } from '@translatr/utils/testing';
import { of } from 'rxjs';

import { LoginPageComponent } from './login-page.component';
import { AuthClientService } from '@dev/translatr-sdk';
import { AuthClient } from '@dev/translatr-model';

describe('LoginPageComponent', () => {
  let component: LoginPageComponent;
  let fixture: ComponentFixture<LoginPageComponent>;
  let authProviderService: AuthClientService & { find: jest.Mock };
  let navigateTo: jest.SpyInstance;

  /**
   * jsdom makes `window.location` non-configurable, so the auto-redirect is observed through the
   * component's `navigateTo` seam. The spy has to be installed between construction and the first
   * change detection, because that is what runs `ngOnInit`.
   */
  const createComponent = (): void => {
    fixture = TestBed.createComponent(LoginPageComponent);
    component = fixture.componentInstance;
    navigateTo = jest.spyOn(component, 'navigateTo').mockImplementation(() => undefined);
    fixture.detectChanges();
  };

  const clientLinks = (): HTMLAnchorElement[] =>
    Array.from(fixture.nativeElement.querySelectorAll('a.client'));

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        declarations: [LoginPageComponent],
        imports: [
          RouterTestingModule,

          NavbarTestingModule,

          // Without it the whole template stays inside an unrendered `*transloco` ng-template.
          TranslocoTestingModule.forRoot({ langs: {}, translocoConfig: { availableLangs: ['en'] } }),

          MatIconModule,
          MatCardModule,
          FontAwesomeTestingModule
        ],
        providers: [
          {
            provide: AuthClientService,
            useFactory: () => ({
              find: jest.fn()
            })
          },
          { provide: ENDPOINT_URL, useValue: '' }
        ]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    authProviderService = TestBed.inject(AuthClientService) as typeof authProviderService;
    authProviderService.find.mockReturnValue(mockObservable());
  });

  it('should create', () => {
    createComponent();

    expect(component).toBeTruthy();
  });

  it('should render a link per provider, including microsoft and apple', () => {
    const providers: AuthClient[] = [
      { key: 'keycloak', url: '/login/keycloak' },
      { key: 'google', url: '/login/google' },
      { key: 'microsoft', url: '/login/microsoft' },
      { key: 'apple', url: '/login/apple' },
      // Not in `names`/`icons` ? must be filtered out rather than rendered blank.
      { key: 'nonesuch', url: '/login/nonesuch' }
    ];
    authProviderService.find.mockReturnValue(of(providers));

    createComponent();

    const links = clientLinks();
    expect(links.map(a => a.getAttribute('href'))).toEqual([
      '/login/keycloak',
      '/login/google',
      '/login/microsoft',
      '/login/apple'
    ]);
    expect(links.map(a => a.textContent.trim())).toEqual([
      'Keycloak',
      'Google',
      'Microsoft',
      'Apple'
    ]);
    // Several providers: the user picks, no auto-redirect.
    expect(navigateTo).not.toHaveBeenCalled();
  });

  it('should redirect automatically when exactly one provider is active', () => {
    authProviderService.find.mockReturnValue(
      of([{ key: 'microsoft', url: '/login/microsoft' }] as AuthClient[])
    );

    createComponent();

    expect(navigateTo).toHaveBeenCalledWith('/login/microsoft');
  });
});
