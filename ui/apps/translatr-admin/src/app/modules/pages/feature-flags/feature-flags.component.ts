import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { FormControl } from '@angular/forms';
import { MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { Router } from '@angular/router';
import { Feature, features, RequestCriteria, ResolvedFeature, User, UserFeatureFlag } from '@dev/translatr-model';
import { isAdmin } from '@dev/translatr-sdk';
import { navigate } from '@translatr/utils';
import { combineLatest, Observable, of, Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, filter, map, switchMap, take, takeUntil, tap } from 'rxjs/operators';
import { AppFacade } from '../../../+state/app.facade';

export interface FeatureRow {
  feature: Feature;
  /** `global ?? defaultEnabled` — the value the user gets with no override. */
  globalDefault: boolean;
  /** Id of the user's override row, if one exists. */
  userOverrideId: string | null;
  /** Effective value for the current user. */
  enabled: boolean;
}

@Component({
  standalone: false,
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'dev-feature-flags',
  templateUrl: './feature-flags.component.html',
  styleUrls: ['./feature-flags.component.scss']
})
export class FeatureFlagsComponent implements OnInit, OnDestroy {
  readonly Feature = Feature;

  private readonly destroy$ = new Subject<void>();

  private readonly me$: Observable<User> = this.facade.me$.pipe(filter(x => !!x));

  readonly isAdmin$: Observable<boolean> = this.me$.pipe(map(isAdmin));

  /** The id of the user whose flags are shown — from `?userId=`, defaulting to the caller. */
  readonly selectedUserId$: Observable<string> = combineLatest([
    this.facade.queryParams$,
    this.me$
  ]).pipe(
    map(([params, me]) => params.userId ?? me.id),
    distinctUntilChanged()
  );

  readonly selectedUser$: Observable<User> = combineLatest([this.selectedUserId$, this.me$]).pipe(
    switchMap(([userId, me]) => (userId === me.id ? of(me) : this.facade.user$(userId)))
  );

  readonly userControl = new FormControl<string | User>('');

  readonly userOptions$: Observable<User[]> = this.userControl.valueChanges.pipe(
    debounceTime(200),
    map(value => (typeof value === 'string' ? value : value?.username)),
    tap(search => this.facade.loadUsers({ search, limit: 10 } as RequestCriteria)),
    switchMap(() => this.facade.users$),
    map(paged => paged?.list ?? [])
  );

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

  constructor(private readonly facade: AppFacade, private readonly router: Router) {}

  ngOnInit(): void {
    combineLatest([this.selectedUserId$, this.me$])
      .pipe(takeUntil(this.destroy$))
      .subscribe(([userId, me]) => {
        // Omit userId entirely for the common "viewing myself" case, rather than passing
        // me.id explicitly — keeps the request identical to the pre-picker shape.
        this.facade.loadResolvedFeatures(userId === me.id ? undefined : userId);
        // Landing on a shared `?userId=` link: the target isn't necessarily in the users$
        // cache yet, so fetch it individually to show their name in the picker.
        if (userId !== me.id) {
          this.facade.loadUser(userId);
        }
      });

    this.selectedUser$.pipe(takeUntil(this.destroy$)).subscribe(user => {
      this.userControl.setValue(user, { emitEvent: false });
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
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

    this.selectedUserId$
      .pipe(take(1))
      .subscribe(userId =>
        this.facade.createFeatureFlag({ userId, feature: row.feature, enabled: desired })
      );
  }

  onUserSelected(event: MatAutocompleteSelectedEvent): void {
    const user = event.option.value as User;
    navigate(this.router, { userId: user.id });
  }

  displayFn(user?: User): string | undefined {
    return user ? user.username : undefined;
  }
}
