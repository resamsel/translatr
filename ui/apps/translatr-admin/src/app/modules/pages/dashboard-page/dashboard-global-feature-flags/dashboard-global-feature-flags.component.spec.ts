import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { TranslocoTestingModule } from '@jsverse/transloco';
import { Feature, features } from '@dev/translatr-model';
import { of, Subject } from 'rxjs';
import { AppActionTypes } from '../../../../+state/app.actions';
import { AppFacade } from '../../../../+state/app.facade';
import { DashboardGlobalFeatureFlagsComponent } from './dashboard-global-feature-flags.component';

describe('DashboardGlobalFeatureFlagsComponent', () => {
  let component: DashboardGlobalFeatureFlagsComponent;
  let fixture: ComponentFixture<DashboardGlobalFeatureFlagsComponent>;
  let facade: any;
  let snackBar: { open: jest.Mock };
  let globalFeatureFlagChanged$: Subject<any>;

  const resolved = [
    { feature: Feature.HeaderGraphic, defaultEnabled: false, global: true, userOverride: null, userOverrideId: null, effective: true },
    { feature: Feature.LanguageSwitcher, defaultEnabled: false, global: null, userOverride: null, userOverrideId: null, effective: false },
    { feature: Feature.ProjectCliCard, defaultEnabled: false, global: null, userOverride: null, userOverrideId: null, effective: false },
    { feature: Feature.ProjectInfographic, defaultEnabled: false, global: null, userOverride: null, userOverrideId: null, effective: false }
  ];
  const global = [{ id: 'g-hg', feature: Feature.HeaderGraphic, enabled: true }];

  beforeEach(
    waitForAsync(() => {
      globalFeatureFlagChanged$ = new Subject<any>();
      snackBar = { open: jest.fn() };
      facade = {
        resolvedFeatures$: of(resolved),
        globalFeatureFlags$: of(global),
        globalFeatureFlagChanged$,
        loadResolvedFeatures: jest.fn(),
        loadGlobalFeatureFlags: jest.fn(),
        setGlobalFeatureFlag: jest.fn(),
        deleteGlobalFeatureFlag: jest.fn()
      };
      TestBed.configureTestingModule({
        declarations: [DashboardGlobalFeatureFlagsComponent],
        imports: [
          NoopAnimationsModule,
          MatButtonModule,
          MatIconModule,
          MatTooltipModule,
          TranslocoTestingModule.forRoot({ langs: {}, translocoConfig: { availableLangs: ['en'] } })
        ],
        providers: [
          { provide: AppFacade, useValue: facade },
          { provide: MatSnackBar, useValue: snackBar }
        ]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(DashboardGlobalFeatureFlagsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('loads resolved + global flags on init', () => {
    expect(facade.loadResolvedFeatures).toHaveBeenCalled();
    expect(facade.loadGlobalFeatureFlags).toHaveBeenCalled();
  });

  it('renders one row per feature with default + effective-global state', done => {
    component.rows$.subscribe(rows => {
      expect(rows.map(r => r.feature)).toEqual(features);
      const hg = rows.find(r => r.feature === Feature.HeaderGraphic);
      expect(hg.defaultEnabled).toBe(false);
      expect(hg.enabled).toBe(true); // global ?? default
      const ls = rows.find(r => r.feature === Feature.LanguageSwitcher);
      expect(ls.enabled).toBe(false);
      done();
    });
  });

  it('toggling ON calls setGlobalFeatureFlag with enabled=true', () => {
    component.onToggle({ feature: Feature.LanguageSwitcher, defaultEnabled: false, enabled: false } as any);
    expect(facade.setGlobalFeatureFlag).toHaveBeenCalledWith(Feature.LanguageSwitcher, true);
  });

  it('toggling OFF calls setGlobalFeatureFlag with enabled=false', () => {
    component.onToggle({ feature: Feature.HeaderGraphic, defaultEnabled: false, enabled: true } as any);
    expect(facade.setGlobalFeatureFlag).toHaveBeenCalledWith(Feature.HeaderGraphic, false);
  });

  it('shows a snackbar when a global feature flag write fails', () => {
    globalFeatureFlagChanged$.next({ type: AppActionTypes.GlobalFeatureFlagSetError });
    expect(snackBar.open).toHaveBeenCalled();
  });
});
