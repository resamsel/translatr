import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Feature, features, GlobalFeatureFlag, ResolvedFeature } from '@dev/translatr-model';
import { combineLatest, Observable, Subject } from 'rxjs';
import { filter, map, takeUntil } from 'rxjs/operators';
import { Action } from '@ngrx/store';
import { AppActionTypes } from '../../../../+state/app.actions';
import { AppFacade } from '../../../../+state/app.facade';

export interface GlobalFeatureRow {
  feature: Feature;
  /** Hardcoded default for this feature. */
  defaultEnabled: boolean;
  /** Effective global state: stored global row ?? hardcoded default. */
  enabled: boolean;
}

@Component({
  standalone: false,
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'dev-dashboard-global-feature-flags',
  templateUrl: './dashboard-global-feature-flags.component.html',
  styleUrls: ['./dashboard-global-feature-flags.component.scss']
})
export class DashboardGlobalFeatureFlagsComponent implements OnInit, OnDestroy {
  private readonly destroy$ = new Subject<void>();

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

  constructor(private readonly facade: AppFacade, readonly snackBar: MatSnackBar) {}

  ngOnInit(): void {
    this.facade.loadResolvedFeatures();
    this.facade.loadGlobalFeatureFlags();

    this.facade.globalFeatureFlagChanged$
      .pipe(
        filter(
          (action: Action) =>
            action.type === AppActionTypes.GlobalFeatureFlagSetError ||
            action.type === AppActionTypes.GlobalFeatureFlagDeleteError
        ),
        takeUntil(this.destroy$)
      )
      .subscribe(() => {
        this.snackBar.open('Global feature flag could not be updated', 'Dismiss', {
          duration: 8000
        });
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onToggle(row: GlobalFeatureRow): void {
    this.facade.setGlobalFeatureFlag(row.feature, !row.enabled);
  }
}
