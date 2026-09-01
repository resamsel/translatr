import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { TranslocoTestingModule } from '@jsverse/transloco';
import { Feature, features } from '@dev/translatr-model';
import { of } from 'rxjs';
import { AppFacade } from '../../../../+state/app.facade';
import { DashboardFeatureFlagsComponent } from './dashboard-feature-flags.component';

describe('DashboardFeatureFlagsComponent', () => {
  let component: DashboardFeatureFlagsComponent;
  let fixture: ComponentFixture<DashboardFeatureFlagsComponent>;
  let facade: any;

  const me = { id: 'user-1' };

  const resolved = [
    // language-switcher: user override ON, global default OFF
    {
      feature: Feature.LanguageSwitcher,
      defaultEnabled: false,
      global: null,
      userOverride: true,
      userOverrideId: 'ff-ls',
      effective: true
    },
    // header-graphic: no override, global ON
    {
      feature: Feature.HeaderGraphic,
      defaultEnabled: false,
      global: true,
      userOverride: null,
      userOverrideId: null,
      effective: true
    },
    // project-cli-card: nothing set, default OFF
    {
      feature: Feature.ProjectCliCard,
      defaultEnabled: false,
      global: null,
      userOverride: null,
      userOverrideId: null,
      effective: false
    },
    {
      feature: Feature.ProjectInfographic,
      defaultEnabled: false,
      global: null,
      userOverride: null,
      userOverrideId: null,
      effective: false
    }
  ];

  beforeEach(
    waitForAsync(() => {
      facade = {
        me$: of(me),
        resolvedFeatures$: of(resolved),
        loadResolvedFeatures: jest.fn(),
        createFeatureFlag: jest.fn(),
        updateFeatureFlag: jest.fn(),
        deleteFeatureFlag: jest.fn()
      };

      TestBed.configureTestingModule({
        declarations: [DashboardFeatureFlagsComponent],
        imports: [
          NoopAnimationsModule,
          MatButtonModule,
          MatIconModule,
          MatTooltipModule,
          TranslocoTestingModule.forRoot({ langs: {}, translocoConfig: { availableLangs: ['en'] } })
        ],
        providers: [{ provide: AppFacade, useValue: facade }]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(DashboardFeatureFlagsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('loads resolved features on init', () => {
    expect(facade.loadResolvedFeatures).toHaveBeenCalled();
  });

  it('renders one row per feature with the global default and effective value', done => {
    component.rows$.subscribe(rows => {
      expect(rows.map(r => r.feature)).toEqual(features);
      const hg = rows.find(r => r.feature === Feature.HeaderGraphic);
      expect(hg.globalDefault).toBe(true); // global ?? defaultEnabled
      expect(hg.enabled).toBe(true); // effective
      const cli = rows.find(r => r.feature === Feature.ProjectCliCard);
      expect(cli.globalDefault).toBe(false);
      done();
    });
  });

  it('CREATE: toggling a feature with no override, away from the default, POSTs enabled=true', () => {
    const row = { feature: Feature.ProjectCliCard, globalDefault: false, userOverrideId: null, enabled: false };
    component.onToggle(row as any);
    expect(facade.createFeatureFlag).toHaveBeenCalledWith({
      userId: 'user-1',
      feature: Feature.ProjectCliCard,
      enabled: true
    });
  });

  it('DELETE: toggling an override back to the global default removes the row', () => {
    const row = { feature: Feature.LanguageSwitcher, globalDefault: false, userOverrideId: 'ff-ls', enabled: true };
    component.onToggle(row as any);
    expect(facade.deleteFeatureFlag).toHaveBeenCalledWith({ id: 'ff-ls' });
    expect(facade.updateFeatureFlag).not.toHaveBeenCalled();
  });

  it('UPDATE: toggling an existing override away from the default flips enabled', () => {
    // header-graphic global ON, imagine the user already had an override row ff-hg = true.
    // globalDefault must be `true` here so that flipping enabled true->false moves AWAY from
    // the default (the UPDATE path); with globalDefault=false this row is identical to the
    // DELETE case and would hit the delete-on-return-to-default branch instead.
    const row = { feature: Feature.HeaderGraphic, globalDefault: true, userOverrideId: 'ff-hg', enabled: true };
    component.onToggle(row as any);
    expect(facade.updateFeatureFlag).toHaveBeenCalledWith({ id: 'ff-hg', feature: Feature.HeaderGraphic, enabled: false });
  });
});
