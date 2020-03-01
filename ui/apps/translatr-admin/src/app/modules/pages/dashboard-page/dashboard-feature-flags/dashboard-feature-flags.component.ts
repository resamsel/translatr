import { Component } from '@angular/core';
import { merge, Observable, of } from 'rxjs';
import { ofType } from '@ngrx/effects';
import { mapTo } from 'rxjs/operators';
import { RequestCriteria, UserFeatureFlag } from '@dev/translatr-model';
import { environment } from '../../../../../environments/environment';
import { AppFacade } from '../../../../+state/app.facade';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Entity, FilterFieldFilter, notifyEvent } from '@dev/translatr-components';
import { errorMessage, hasDeleteAllFeatureFlagsPermission, hasDeleteFeatureFlagPermission } from '@dev/translatr-sdk';
import {
  AppActionTypes,
  FeatureFlagDeleted,
  FeatureFlagDeleteError,
  FeatureFlagsDeleted,
  FeatureFlagsDeleteError
} from '../../../../+state/app.actions';

@Component({
  selector: 'dev-dashboard-feature-flags',
  templateUrl: './dashboard-feature-flags.component.html',
  styleUrls: ['./dashboard-feature-flags.component.scss']
})
export class DashboardFeatureFlagsComponent {
  displayedColumns = ['user', 'featureFlag', 'enabled', 'actions'];

  me$ = this.facade.me$;
  featureFlags$ = this.facade.featureFlags$;
  load$ = merge(
    of({limit: '20', order: 'featureFlag asc'}),
    this.facade.featureFlagDeleted$.pipe(
      ofType(AppActionTypes.FeatureFlagDeleted),
      mapTo({})
    ),
    this.facade.featureFlagsDeleted$.pipe(
      ofType(AppActionTypes.FeatureFlagsDeleted),
      mapTo({})
    )
  );

  selected: UserFeatureFlag[] = [];

  readonly uiUrl = environment.uiUrl;

  readonly filters: Array<FilterFieldFilter> = [{
    key: 'featureFlag',
    type: 'option',
    title: 'Feature flag',
    value: 'project-cli-card'
  }];

  constructor(
    private readonly facade: AppFacade,
    readonly snackBar: MatSnackBar
  ) {
    notifyEvent(
      snackBar,
      facade.featureFlagDeleted$,
      AppActionTypes.FeatureFlagDeleted,
      (action: FeatureFlagDeleted) =>
        `Feature flag ${action.payload.featureFlag} has been deleted`,
      (action: FeatureFlagDeleteError) =>
        `Feature flag could not be deleted: ${errorMessage(action.payload)}`
    );
    notifyEvent(
      snackBar,
      facade.featureFlagsDeleted$,
      AppActionTypes.FeatureFlagsDeleted,
      (action: FeatureFlagsDeleted) =>
        `${action.payload.length} feature flags have been deleted`,
      (action: FeatureFlagsDeleteError) =>
        `Feature flags could not be deleted: ${errorMessage(action.payload)}`
    );
  }

  onSelected(entities: Entity[]) {
    this.selected = entities as UserFeatureFlag[];
  }

  onCriteriaChanged(criteria: RequestCriteria) {
    this.facade.loadFeatureFlags(criteria);
  }

  allowDelete$(featureFlag: UserFeatureFlag): Observable<boolean> {
    return this.me$.pipe(hasDeleteFeatureFlagPermission(featureFlag));
  }

  onDelete(featureFlag: UserFeatureFlag) {
    this.facade.deleteFeatureFlag(featureFlag);
  }

  allowDeleteAll$(featureFlags: UserFeatureFlag[]): Observable<boolean> {
    return this.me$.pipe(hasDeleteAllFeatureFlagsPermission(featureFlags));
  }

  onDeleteAll(featureFlags: UserFeatureFlag[]) {
    this.facade.deleteFeatureFlags(featureFlags);
  }

  onEnable(featureFlag: UserFeatureFlag): void {
    this.facade.updateFeatureFlag({
      ...featureFlag,
      enabled: true
    });
  }

  onDisable(featureFlag: UserFeatureFlag): void {
    this.facade.updateFeatureFlag({
      ...featureFlag,
      enabled: false
    });
  }
}
