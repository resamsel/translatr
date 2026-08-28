import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { Feature, features, UserFeatureFlag } from '@dev/translatr-model';
import { combineLatest, Observable } from 'rxjs';
import { filter, map, take } from 'rxjs/operators';
import { AppFacade } from '../../../../+state/app.facade';

export interface FeatureRow {
  feature: Feature;
  /** The current user's flag row for this feature, if one exists yet. */
  flag?: UserFeatureFlag;
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

  /** One row per available Feature, joined with the current user's stored flags. */
  readonly rows$: Observable<FeatureRow[]> = combineLatest([
    this.facade.featureFlags$,
    this.facade.me$
  ]).pipe(
    map(([page, me]) =>
      features.map(feature => {
        const flag = (page?.list ?? []).find(
          f => f.feature === feature && (!me || f.userId === me.id)
        );
        return { feature, flag, enabled: !!flag?.enabled };
      })
    )
  );

  constructor(private readonly facade: AppFacade) {}

  ngOnInit(): void {
    this.me$
      .pipe(take(1))
      .subscribe(me => this.facade.loadFeatureFlags({ userId: me.id }));
  }

  onToggle(row: FeatureRow): void {
    if (row.flag) {
      this.facade.updateFeatureFlag({ ...row.flag, enabled: !row.flag.enabled });
      return;
    }
    this.me$
      .pipe(take(1))
      .subscribe(me =>
        this.facade.createFeatureFlag({ userId: me.id, feature: row.feature, enabled: true })
      );
  }

  onDelete(row: FeatureRow): void {
    if (row.flag) {
      this.facade.deleteFeatureFlag(row.flag);
    }
  }

  ngOnDestroy(): void {
    this.facade.unloadFeatureFlags();
  }
}
