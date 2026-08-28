import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { Feature, features } from '@dev/translatr-model';
import { of } from 'rxjs';
import { AppFacade } from '../../../../+state/app.facade';
import { DashboardFeatureFlagsComponent } from './dashboard-feature-flags.component';

describe('DashboardFeatureFlagsComponent', () => {
  let component: DashboardFeatureFlagsComponent;
  let fixture: ComponentFixture<DashboardFeatureFlagsComponent>;
  let facade: {
    me$: any;
    featureFlags$: any;
    loadFeatureFlags: jest.Mock;
    createFeatureFlag: jest.Mock;
    updateFeatureFlag: jest.Mock;
    unloadFeatureFlags: jest.Mock;
  };

  const me = { id: 'user-1' };

  beforeEach(
    waitForAsync(() => {
      facade = {
        me$: of(me),
        featureFlags$: of({ list: [], total: 0, offset: 0, limit: 20 }),
        loadFeatureFlags: jest.fn(),
        createFeatureFlag: jest.fn(),
        updateFeatureFlag: jest.fn(),
        unloadFeatureFlags: jest.fn()
      };

      TestBed.configureTestingModule({
        declarations: [DashboardFeatureFlagsComponent],
        imports: [NoopAnimationsModule, MatButtonModule, MatIconModule, MatTooltipModule],
        providers: [{ provide: AppFacade, useValue: facade }]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(DashboardFeatureFlagsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('loads the current user\'s feature flags on init', () => {
    expect(facade.loadFeatureFlags).toHaveBeenCalledWith({ userId: 'user-1' });
  });

  it('renders one row per available feature', done => {
    component.rows$.subscribe(rows => {
      expect(rows.map(r => r.feature)).toEqual(features);
      expect(rows.every(r => r.enabled === false)).toBe(true);
      done();
    });
  });

  it('creates a flag when toggling a feature that has no row yet', () => {
    component.onToggle({ feature: Feature.ProjectCliCard, enabled: false });

    expect(facade.createFeatureFlag).toHaveBeenCalledWith({
      userId: 'user-1',
      feature: Feature.ProjectCliCard,
      enabled: true
    });
    expect(facade.updateFeatureFlag).not.toHaveBeenCalled();
  });

  it('updates the existing row when toggling a feature that already has one', () => {
    const flag = {
      id: 'ff-1',
      userId: 'user-1',
      feature: Feature.ProjectInfographic,
      enabled: true
    } as any;

    component.onToggle({ feature: Feature.ProjectInfographic, flag, enabled: true });

    expect(facade.updateFeatureFlag).toHaveBeenCalledWith({ ...flag, enabled: false });
    expect(facade.createFeatureFlag).not.toHaveBeenCalled();
  });
});
