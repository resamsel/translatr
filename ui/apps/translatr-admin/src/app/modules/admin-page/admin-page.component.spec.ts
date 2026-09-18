import { BreakpointObserver } from '@angular/cdk/layout';
import { Component } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';
import { MatSidenav, MatSidenavContainer, MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { By } from '@angular/platform-browser';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { FeatureFlagModule, ThemeService } from '@dev/translatr-components';
import { FeatureFlagFacade } from '@dev/translatr-model';
import { of } from 'rxjs';
import { AppFacade } from '../../+state/app.facade';
import { DASHBOARD_ROUTES } from '../pages/dashboard-page/dashboard-page.token';
import { SidenavTestingModule } from '../nav/testing';
import { AdminPageComponent } from './admin-page.component';

@Component({
  standalone: false,
  template: `
    <dev-admin-page [headerColor]="'#e83a5f'">
      <p>content</p>
    </dev-admin-page>
  `
})
class HostComponent {}

describe('AdminPageComponent', () => {
  let fixture: ComponentFixture<HostComponent>;

  const createComponent = (
    largeScreen: boolean,
    themeFlagEnabled = false,
    preference = 'system'
  ) => {
    const breakpointObserver: Partial<BreakpointObserver> = {
      isMatched: () => largeScreen,
      observe: () => of({ matches: largeScreen, breakpoints: {} })
    };

    TestBed.configureTestingModule({
      declarations: [HostComponent, AdminPageComponent],
      imports: [
        SidenavTestingModule,
        FeatureFlagModule,

        RouterTestingModule,
        NoopAnimationsModule,

        MatSidenavModule,
        MatToolbarModule,
        MatButtonModule,
        MatIconModule,
        MatListModule,
        MatMenuModule
      ],
      providers: [
        { provide: AppFacade, useFactory: () => ({}) },
        {
          provide: FeatureFlagFacade,
          useFactory: () => ({ hasFeatures$: () => of(themeFlagEnabled) })
        },
        {
          provide: ThemeService,
          useFactory: () => ({
            preference$: of(preference),
            effectiveTheme$: of(preference === 'dark' ? 'dark' : 'light'),
            setPreference: jest.fn()
          })
        },
        { provide: BreakpointObserver, useValue: breakpointObserver },
        { provide: DASHBOARD_ROUTES, useValue: [] }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(HostComponent);
    fixture.detectChanges();
  };

  const drawer = (): MatSidenav =>
    fixture.debugElement.query(By.directive(MatSidenav)).componentInstance;

  const drawerContainer = (): MatSidenavContainer =>
    fixture.debugElement.query(By.directive(MatSidenavContainer)).componentInstance;

  it('should create', waitForAsync(() => {
    createComponent(true);
    expect(fixture.componentInstance).toBeTruthy();
  }));

  it('docks the sidebar open beside the content on large screens', waitForAsync(() => {
    createComponent(true);

    expect(drawer().mode).toBe('side');
    expect(drawer().opened).toBe(true);
  }));

  it('keeps the sidebar as a closed overlay below the large breakpoint', waitForAsync(() => {
    createComponent(false);

    expect(drawer().mode).toBe('over');
    expect(drawer().opened).toBe(false);
  }));

  it('lets the container dock the docked sidebar without a backdrop on large screens', waitForAsync(() => {
    createComponent(true);

    expect(drawerContainer().hasBackdrop).toBe(false);
  }));

  it('lets the container back the overlay sidebar with a backdrop on small screens', waitForAsync(() => {
    createComponent(false);

    expect(drawerContainer().hasBackdrop).toBe(true);
  }));

  it('passes the page-provided headerColor through to the navbar', waitForAsync(() => {
    createComponent(true);

    const sidenav = fixture.debugElement.query(By.css('app-sidenav'));
    expect(sidenav.componentInstance.headerColor).toBe('#e83a5f');
  }));

  it('projects the page content', waitForAsync(() => {
    createComponent(true);

    expect(fixture.debugElement.query(By.css('.content p')).nativeElement.textContent).toBe(
      'content'
    );
  }));

  describe('theme toggle', () => {
    it('hides the theme toggle button when the ThemeSwitcher feature flag is disabled', waitForAsync(() => {
      createComponent(true, false);

      expect(fixture.debugElement.query(By.css('.theme-toggle-button'))).toBeFalsy();
    }));

    it('shows the theme toggle button in the toolbar when the flag is enabled', waitForAsync(() => {
      createComponent(true, true);

      expect(fixture.debugElement.query(By.css('.theme-toggle-button'))).toBeTruthy();
    }));

    it('applies the selection via ThemeService when an admin picks "Dark" from the menu', waitForAsync(() => {
      createComponent(true, true);
      const themeService = TestBed.inject(ThemeService);

      const adminPage = fixture.debugElement.query(
        By.directive(AdminPageComponent)
      ).componentInstance as AdminPageComponent;
      adminPage.onThemeChange('dark' as never);

      expect(themeService.setPreference).toHaveBeenCalledWith('dark');
    }));
  });
});
