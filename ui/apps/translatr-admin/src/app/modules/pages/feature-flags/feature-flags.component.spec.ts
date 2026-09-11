import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatInputModule } from '@angular/material/input';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { TranslocoTestingModule } from '@jsverse/transloco';
import { Feature, features, UserRole } from '@dev/translatr-model';
import { of } from 'rxjs';
import { AppFacade } from '../../../+state/app.facade';
import { AdminPageTestingModule } from '../../admin-page/testing';
import { FeatureFlagsComponent } from './feature-flags.component';

describe('FeatureFlagsComponent', () => {
  let component: FeatureFlagsComponent;
  let fixture: ComponentFixture<FeatureFlagsComponent>;
  let facade: any;
  let router: { navigate: jest.Mock };

  const me = { id: 'user-1', username: 'me', role: UserRole.User };
  const admin = { id: 'admin-1', username: 'admin', role: UserRole.Admin };

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

  function configure(loggedInUser: any, queryParams: any = {}) {
    facade = {
      me$: of(loggedInUser),
      queryParams$: of(queryParams),
      resolvedFeatures$: of(resolved),
      users$: of({ list: [], offset: 0, limit: 10, hasNext: false, hasPrev: false }),
      user$: jest.fn(() => of(undefined)),
      loadUsers: jest.fn(),
      loadUser: jest.fn(),
      loadResolvedFeatures: jest.fn(),
      createFeatureFlag: jest.fn(),
      updateFeatureFlag: jest.fn(),
      deleteFeatureFlag: jest.fn()
    };
    router = { navigate: jest.fn() };

    TestBed.configureTestingModule({
      declarations: [FeatureFlagsComponent],
      imports: [
        AdminPageTestingModule,
        NoopAnimationsModule,
        ReactiveFormsModule,
        MatAutocompleteModule,
        MatInputModule,
        MatSlideToggleModule,
        MatTableModule,
        MatTooltipModule,
        TranslocoTestingModule.forRoot({ langs: {}, translocoConfig: { availableLangs: ['en'] } })
      ],
      providers: [
        { provide: AppFacade, useValue: facade },
        { provide: Router, useValue: router }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(FeatureFlagsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  describe('as a non-admin', () => {
    beforeEach(waitForAsync(() => configure(me)));

    it('loads resolved features for its own id, with no picker shown', () => {
      // Omitted (not 'user-1') so the request shape matches the pre-picker default exactly.
      expect(facade.loadResolvedFeatures).toHaveBeenCalledWith(undefined);
      component.isAdmin$.subscribe(value => expect(value).toBe(false));
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

  describe('as an admin, with no ?userId= in the URL', () => {
    beforeEach(waitForAsync(() => configure(admin)));

    it('defaults the picker to its own id', () => {
      expect(facade.loadResolvedFeatures).toHaveBeenCalledWith(undefined);
      expect(facade.loadUser).not.toHaveBeenCalled();
      component.isAdmin$.subscribe(value => expect(value).toBe(true));
    });

    it('toggling targets its own id', () => {
      const row = { feature: Feature.ProjectCliCard, globalDefault: false, userOverrideId: null, enabled: false };
      component.onToggle(row as any);
      expect(facade.createFeatureFlag).toHaveBeenCalledWith({
        userId: 'admin-1',
        feature: Feature.ProjectCliCard,
        enabled: true
      });
    });
  });

  describe('as an admin, with ?userId= for someone else', () => {
    beforeEach(waitForAsync(() => configure(admin, { userId: 'other-1' })));

    it('loads resolved features for the targeted user and fetches their details', () => {
      expect(facade.loadResolvedFeatures).toHaveBeenCalledWith('other-1');
      expect(facade.loadUser).toHaveBeenCalledWith('other-1');
    });

    it('toggling targets the selected user, not the caller', () => {
      const row = { feature: Feature.ProjectCliCard, globalDefault: false, userOverrideId: null, enabled: false };
      component.onToggle(row as any);
      expect(facade.createFeatureFlag).toHaveBeenCalledWith({
        userId: 'other-1',
        feature: Feature.ProjectCliCard,
        enabled: true
      });
    });
  });

  describe('picking a user from the autocomplete', () => {
    beforeEach(waitForAsync(() => configure(admin)));

    it('navigates with ?userId= set to the selected user, sharable as a link', () => {
      const selected = { id: 'picked-1', username: 'picked' };
      component.onUserSelected({ option: { value: selected } } as any);
      expect(router.navigate).toHaveBeenCalledWith(
        [],
        expect.objectContaining({ queryParamsHandling: 'merge', queryParams: { userId: 'picked-1' } })
      );
    });

    it('displayFn shows the username', () => {
      expect(component.displayFn({ id: '1', username: 'someone' } as any)).toBe('someone');
      expect(component.displayFn(undefined)).toBeUndefined();
    });
  });
});
