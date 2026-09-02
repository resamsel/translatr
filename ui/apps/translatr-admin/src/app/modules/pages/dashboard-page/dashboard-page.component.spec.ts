import { BreakpointObserver } from '@angular/cdk/layout';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatSidenav, MatSidenavContainer, MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { By } from '@angular/platform-browser';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';
import { AppFacade } from '../../../+state/app.facade';
import { SidenavTestingModule } from '../../testing';
import { DashboardPageComponent } from './dashboard-page.component';
import { DASHBOARD_ROUTES } from './dashboard-page.token';

describe('DashboardPageComponent', () => {
  let component: DashboardPageComponent;
  let fixture: ComponentFixture<DashboardPageComponent>;

  const createComponent = (largeScreen: boolean) => {
    const breakpointObserver: Partial<BreakpointObserver> = {
      isMatched: () => largeScreen,
      observe: () => of({ matches: largeScreen, breakpoints: {} })
    };

    TestBed.configureTestingModule({
      declarations: [DashboardPageComponent],
      imports: [
        SidenavTestingModule,

        RouterTestingModule,
        NoopAnimationsModule,

        MatSidenavModule,
        MatToolbarModule,
        MatButtonModule,
        MatIconModule,
        MatListModule
      ],
      providers: [
        { provide: AppFacade, useFactory: () => ({}) },
        { provide: BreakpointObserver, useValue: breakpointObserver },
        {
          provide: DASHBOARD_ROUTES,
          useValue: [{ children: [] }]
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  };

  const drawer = (): MatSidenav =>
    fixture.debugElement.query(By.directive(MatSidenav)).componentInstance;

  const drawerContainer = (): MatSidenavContainer =>
    fixture.debugElement.query(By.directive(MatSidenavContainer)).componentInstance;

  it('should create', waitForAsync(() => {
    createComponent(true);
    expect(component).toBeTruthy();
  }));

  const drawerToolbar = () => fixture.debugElement.query(By.css('mat-sidenav.sidenav mat-toolbar'));

  it('docks the sidebar open beside the content on large screens', waitForAsync(() => {
    createComponent(true);

    expect(drawer().mode).toBe('side');
    expect(drawer().opened).toBe(true);
  }));

  it('drops the redundant in-drawer toolbar when the sidebar is docked', waitForAsync(() => {
    createComponent(true);

    expect(drawerToolbar()).toBeNull();
  }));

  it('keeps the sidebar as a closed overlay below the large breakpoint', waitForAsync(() => {
    createComponent(false);

    expect(drawer().mode).toBe('over');
    expect(drawer().opened).toBe(false);
  }));

  it('keeps the in-drawer toolbar as the close affordance on small screens', waitForAsync(() => {
    createComponent(false);

    expect(drawerToolbar()).not.toBeNull();
  }));

  // Regression guard: the drawer must be a real content child of the container,
  // otherwise Material never pushes the content and the sidebar just overlays it
  // (issue #243). `hasBackdrop` is derived from the drawer, so it only reflects
  // the mode when the container actually sees the drawer.
  it('lets the container dock the docked sidebar without a backdrop on large screens', waitForAsync(() => {
    createComponent(true);

    expect(drawerContainer().hasBackdrop).toBe(false);
  }));

  it('lets the container back the overlay sidebar with a backdrop on small screens', waitForAsync(() => {
    createComponent(false);

    expect(drawerContainer().hasBackdrop).toBe(true);
  }));
});
