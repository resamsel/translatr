import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { Feature, features, GlobalFeatureFlag, ResolvedFeature } from '@dev/translatr-model';
import { combineLatest, Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { AppFacade } from '../../../../+state/app.facade';

export interface GlobalFeatureRow {
  feature: Feature;
  /** Hardcoded default for this feature. */
  defaultEnabled: boolean;
  /** Effective global state: stored global row ?? hardcoded default. */
  enabled: boolean;
}

const featureNames: Record<Feature, string> = {
  [Feature.ProjectCliCard]: 'Project CLI integration card',
  [Feature.ProjectInfographic]: 'Project infographic',
  [Feature.HeaderGraphic]: 'Header graphic',
  [Feature.LanguageSwitcher]: 'Language switcher'
};

@Component({
  standalone: false,
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'dev-dashboard-global-feature-flags',
  templateUrl: './dashboard-global-feature-flags.component.html',
  styleUrls: ['./dashboard-global-feature-flags.component.scss']
})
export class DashboardGlobalFeatureFlagsComponent implements OnInit {
  readonly featureNames = featureNames;

  readonly rows$: Observable<GlobalFeatureRow[]> = combineLatest([
    this.facade.resolvedFeatures$,
    this.facade.globalFeatureFlags$
  ]).pipe(
    map(([resolved, global]: [ResolvedFeature[] | undefined, GlobalFeatureFlag[] | undefined]) =>
      features.map(feature => {
        const r = (resolved ?? []).find(x => x.feature === feature);
        const g = (global ?? []).find(x => x.feature === feature);
        const defaultEnabled = r ? r.defaultEnabled : false;
        return {
          feature,
          defaultEnabled,
          enabled: g ? g.enabled : defaultEnabled
        };
      })
    )
  );

  constructor(private readonly facade: AppFacade) {}

  ngOnInit(): void {
    this.facade.loadResolvedFeatures();
    this.facade.loadGlobalFeatureFlags();
  }

  onToggle(row: GlobalFeatureRow): void {
    this.facade.setGlobalFeatureFlag(row.feature, !row.enabled);
  }
}
