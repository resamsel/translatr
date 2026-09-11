import { Component } from '@angular/core';
import { fakeAsync, TestBed, tick } from '@angular/core/testing';
import { Route, Router, Routes } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { User, UserRole } from '@dev/translatr-model';
import { LOGIN_URL, WINDOW } from '@translatr/utils';
import { of } from 'rxjs';
import { AppFacade } from '../../../+state/app.facade';
import { routes } from './dashboard-page-routing.module';

@Component({ standalone: false, template: '' })
class StubComponent {}

/**
 * Swap every real page component for a stub so the route tree (paths + guards)
 * can be exercised without pulling in Material, dialogs and the SDK.
 */
const stub = (input: Routes): Routes =>
  input.map((route: Route) => {
    const next: Route = { ...route };
    if (next.component) {
      next.component = StubComponent;
    }
    if (next.loadChildren) {
      delete next.loadChildren;
      next.component = StubComponent;
    }
    if (next.children) {
      next.children = stub(next.children);
    }
    return next;
  });

const PAGE_PATHS = [
  '/',
  '/users',
  '/users/42',
  '/projects',
  '/accesstokens',
  '/health',
  '/featureflags',
  '/featureflags/global'
];

describe('admin route authorization', () => {
  let router: Router;
  let facade: { loadMe: jest.Mock; me$: unknown; loggedInUserSettled$: unknown };

  const configure = (user: User | null) => {
    facade = {
      loadMe: jest.fn(),
      me$: of(user),
      loggedInUserSettled$: of({ type: '[Translatr API] Logged-In User Loaded' })
    };

    TestBed.configureTestingModule({
      declarations: [StubComponent],
      imports: [
        RouterTestingModule.withRoutes([
          { path: 'forbidden', component: StubComponent },
          ...stub(routes)
        ])
      ],
      providers: [
        { provide: AppFacade, useValue: facade },
        { provide: WINDOW, useValue: { location: { href: '' } } },
        { provide: LOGIN_URL, useValue: '' }
      ]
    });

    router = TestBed.inject(Router);
  };

  afterEach(() => TestBed.resetTestingModule());

  describe('a non-admin user', () => {
    PAGE_PATHS.forEach(path => {
      it(`is redirected to /forbidden when opening ${path}`, fakeAsync(() => {
        configure({ id: '2', role: UserRole.User } as User);

        router.navigateByUrl(path);
        tick();

        expect(router.url).toBe(`/forbidden?path=${encodeURIComponent(path)}`);
      }));
    });

    it('is redirected to /forbidden for an unknown path (wildcard falls back to the guarded shell)', fakeAsync(() => {
      configure({ id: '2', role: UserRole.User } as User);

      router.navigateByUrl('/does-not-exist');
      tick();

      expect(router.url).toBe(`/forbidden?path=${encodeURIComponent('/')}`);
    }));

    it('can still reach /forbidden itself without a redirect loop', fakeAsync(() => {
      configure({ id: '2', role: UserRole.User } as User);

      router.navigateByUrl('/forbidden');
      tick();

      expect(router.url).toBe('/forbidden');
    }));
  });

  describe('an admin user', () => {
    PAGE_PATHS.forEach(path => {
      it(`can open ${path}`, fakeAsync(() => {
        configure({ id: '1', role: UserRole.Admin } as User);

        router.navigateByUrl(path);
        tick();

        expect(router.url).toBe(path === '/' ? '/' : path);
      }));
    });

    it('lands on the dashboard for an unknown path', fakeAsync(() => {
      configure({ id: '1', role: UserRole.Admin } as User);

      router.navigateByUrl('/nope');
      tick();

      expect(router.url).toBe('/');
    }));
  });
});
