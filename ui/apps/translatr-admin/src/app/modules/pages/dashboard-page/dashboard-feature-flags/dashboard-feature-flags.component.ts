import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { Feature, features, ResolvedFeature, UserFeatureFlag } from '@dev/translatr-model';
import { Observable } from 'rxjs';
import { filter, map, take } from 'rxjs/operators';
import { AppFacade } from '../../../../+state/app.facade';

export interface FeatureRow {
  feature: Feature;
  /** `global ?? defaultEnabled` — the value the user gets with no override. */
  globalDefault: boolean;
  /** Id of the user's override row, if one exists. */
  userOverrideId: string | null;
  /** Effective value for the current user. */
  enabled: boolean;
}

/** Human-readable labels for the {@link Feature} enum values. */
const featureNames: Record<Feature, string> = {
  [Feature.ProjectCliCard]: 'Project CLI integration card',
  [Feature.ProjectInfographic]: 'Project infographic',
  [Feature.HeaderGraphic]: 'Header graphic',
  [Feature.LanguageSwitcher]: 'Language switcher'
};

@Component({
  standalone: false,
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'dev-dashboard-feature-flags',
  templateUrl: './dashboard-feature-flags.component.html',
  styleUrls: ['./dashboard-feature-flags.component.scss']
})
export class DashboardFeatureFlagsComponent implements OnInit, OnDestroy {
  readonly Feature = Feature;
  readonly featureNames = featureNames;

  private readonly me$ = this.facade.me$.pipe(filter(x => !!x));

  readonly rows$: Observable<FeatureRow[]> = this.facade.resolvedFeatures$.pipe(
    map((resolved: ResolvedFeature[] | undefined) =>
      features.map(feature => {
        const r = (resolved ?? []).find(x => x.feature === feature);
        const globalDefault = r ? (r.global ?? r.defaultEnabled) : false;
        return {
          feature,
          globalDefault,
          userOverrideId: r?.userOverrideId ?? null,
          enabled: r ? r.effective : false
        };
      })
    )
  );

  constructor(private readonly facade: AppFacade) {}

  ngOnInit(): void {
    this.facade.loadResolvedFeatures();
  }

  onToggle(row: FeatureRow): void {
    const desired = !row.enabled;

    // Returning to the value the user would get anyway → drop the override entirely.
    if (desired === row.globalDefault) {
      if (row.userOverrideId) {
        this.facade.deleteFeatureFlag({ id: row.userOverrideId } as UserFeatureFlag);
      }
      return;
    }

    if (row.userOverrideId) {
      this.facade.updateFeatureFlag({
        id: row.userOverrideId,
        feature: row.feature,
        enabled: desired
      } as UserFeatureFlag);
      return;
    }

    this.me$
      .pipe(take(1))
      .subscribe(me =>
        this.facade.createFeatureFlag({ userId: me.id, feature: row.feature, enabled: desired })
      );
  }

  ngOnDestroy(): void {
    this.facade.unloadFeatureFlags();
  }
}
