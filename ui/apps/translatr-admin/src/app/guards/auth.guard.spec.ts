import { TestBed } from '@angular/core/testing';
import { Router, RouterStateSnapshot } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { User, UserRole } from '@dev/translatr-model';
import { LOGIN_URL, WINDOW } from '@translatr/utils';
import { EMPTY, of, Subject, throwError } from 'rxjs';
import { AppFacade } from '../+state/app.facade';
import { environment } from '../../environments/environment';
import { AuthGuard } from './auth.guard';

const adminUser = { id: '1', role: UserRole.Admin } as User;
const normalUser = { id: '2', role: UserRole.User } as User;

const routeState = (url: string) => ({ url } as RouterStateSnapshot);

describe('AuthGuard', () => {
  let guard: AuthGuard;
  let router: Router;
  let loadMe: jest.Mock;
  let windowRef: { location: { href: string } };
  let facade: {
    loadMe: jest.Mock;
    me$: unknown;
    loggedInUserSettled$: unknown;
  };

  const configure = (
    overrides: Partial<{ me$: unknown; loggedInUserSettled$: unknown }> = {},
    loginUrl = ''
  ) => {
    loadMe = jest.fn();
    windowRef = { location: { href: 'http://localhost:4211/admin' } };
    facade = {
      loadMe,
      me$: of(undefined),
      loggedInUserSettled$: of({ type: '[Translatr API] Logged-In User Loaded' }),
      ...overrides
    };

    TestBed.configureTestingModule({
      imports: [RouterTestingModule],
      providers: [
        AuthGuard,
        { provide: AppFacade, useValue: facade },
        { provide: WINDOW, useValue: windowRef },
        { provide: LOGIN_URL, useValue: loginUrl }
      ]
    });

    guard = TestBed.inject(AuthGuard);
    router = TestBed.inject(Router);
    jest.spyOn(router, 'navigate').mockResolvedValue(true);
  };

  it('should create', () => {
    configure();
    expect(guard).toBeTruthy();
  });

  it('allows navigation for a resolved admin user', done => {
    configure({ me$: of(adminUser) });

    guard.canActivate(null, routeState('/users')).subscribe(result => {
      expect(result).toBe(true);
      expect(loadMe).toHaveBeenCalled();
      expect(router.navigate).not.toHaveBeenCalled();
      done();
    });
  });

  it('denies a resolved non-admin user and redirects to /forbidden with the attempted path', done => {
    configure({ me$: of(normalUser) });

    guard.canActivate(null, routeState('/projects')).subscribe(result => {
      expect(result).toBe(false);
      expect(router.navigate).toHaveBeenCalledWith(['/forbidden'], {
        queryParams: { path: '/projects' }
      });
      done();
    });
  });

  it('applies the same check via canActivateChild', done => {
    configure({ me$: of(normalUser) });

    guard.canActivateChild(null, routeState('/health')).subscribe(result => {
      expect(result).toBe(false);
      expect(router.navigate).toHaveBeenCalledWith(['/forbidden'], {
        queryParams: { path: '/health' }
      });
      done();
    });
  });

  it('redirects an unauthenticated visitor to the login URL with a redirect_uri', done => {
    configure({ me$: of(null) }, 'http://localhost:4210/ui/login');

    guard.canActivate(null, routeState('/accesstokens')).subscribe(result => {
      expect(result).toBe(false);
      expect(windowRef.location.href).toBe(
        `http://localhost:4210/ui/login?redirect_uri=${encodeURIComponent(
          environment.adminUrl + '/accesstokens'
        )}`
      );
      done();
    });
  });

  it('fails closed when the user lookup errors (no user -> login)', done => {
    configure({ me$: EMPTY, loggedInUserSettled$: throwError(() => new Error('load failed')) });

    guard.canActivate(null, routeState('/users')).subscribe(result => {
      expect(result).toBe(false);
      expect(router.navigate).toHaveBeenCalledWith(
        [''],
        expect.objectContaining({
          queryParams: { redirect_uri: environment.adminUrl + '/users' }
        })
      );
      done();
    });
  });

  it('fails closed when the settle signal never arrives (timeout)', () => {
    jest.useFakeTimers();
    try {
      configure({ me$: of(undefined), loggedInUserSettled$: new Subject() });

      let result: boolean | undefined;
      guard.canActivate(null, routeState('/users')).subscribe(value => (result = value));

      jest.advanceTimersByTime(11000);

      expect(result).toBe(false);
      expect(router.navigate).toHaveBeenCalled();
    } finally {
      jest.useRealTimers();
    }
  });
});
