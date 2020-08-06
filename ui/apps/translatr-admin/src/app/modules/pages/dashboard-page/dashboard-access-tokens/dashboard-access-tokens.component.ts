import { ChangeDetectionStrategy, Component, OnDestroy } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Entity, notifyEvent } from '@dev/translatr-components';
import { AccessToken, Feature, RequestCriteria } from '@dev/translatr-model';
import {
  errorMessage,
  hasDeleteAccessTokenPermission,
  hasDeleteAllAccessTokensPermission,
  hasEditAccessTokenPermission
} from '@dev/translatr-sdk';
import { ofType } from '@ngrx/effects';
import { merge, Observable, of } from 'rxjs';
import { mapTo, takeUntil } from 'rxjs/operators';
import {
  AccessTokenDeleted,
  AccessTokenDeleteError,
  AccessTokensDeleted,
  AccessTokensDeleteError,
  AppActionTypes
} from '../../../../+state/app.actions';
import { AppFacade } from '../../../../+state/app.facade';
import { environment } from '../../../../../environments/environment';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'dev-dashboard-access-tokens',
  templateUrl: './dashboard-access-tokens.component.html',
  styleUrls: ['./dashboard-access-tokens.component.scss']
})
export class DashboardAccessTokensComponent implements OnDestroy {
  displayedColumns = ['name', 'user', 'scopes', 'when_created', 'actions'];

  me$ = this.facade.me$;
  accessTokens$ = this.facade.accessTokens$;
  load$ = merge(
    of({ limit: '20', order: 'whenCreated desc' }),
    this.facade.accessTokenDeleted$.pipe(ofType(AppActionTypes.AccessTokenDeleted), mapTo({})),
    this.facade.accessTokensDeleted$.pipe(ofType(AppActionTypes.AccessTokensDeleted), mapTo({}))
  );

  selected: AccessToken[] = [];

  readonly uiUrl = environment.uiUrl;

  readonly Feature = Feature;

  constructor(private readonly facade: AppFacade, readonly snackBar: MatSnackBar) {
    notifyEvent(
      snackBar,
      facade.accessTokenDeleted$.pipe(takeUntil(facade.unloadAccessTokens$)),
      AppActionTypes.AccessTokenDeleted,
      (action: AccessTokenDeleted) => `Access token ${action.payload.name} has been deleted`,
      (action: AccessTokenDeleteError) =>
        `Access token could not be deleted: ${errorMessage(action.payload)}`
    );
    notifyEvent(
      snackBar,
      facade.accessTokensDeleted$.pipe(takeUntil(facade.unloadAccessTokens$)),
      AppActionTypes.AccessTokensDeleted,
      (action: AccessTokensDeleted) => `${action.payload.length} access tokens have been deleted`,
      (action: AccessTokensDeleteError) =>
        `Access tokens could not be deleted: ${errorMessage(action.payload)}`
    );
  }

  onSelected(entities: Entity[]) {
    this.selected = entities as AccessToken[];
  }

  onCriteriaChanged(criteria: RequestCriteria) {
    this.facade.loadAccessTokens(criteria);
  }

  allowEdit$(accessToken: AccessToken): Observable<boolean> {
    return this.me$.pipe(hasEditAccessTokenPermission(accessToken));
  }

  onEdit(accessToken: AccessToken) {
    // TODO: implement
    // this.facade.deleteAccessToken(accessToken);
  }

  allowDelete$(accessToken: AccessToken): Observable<boolean> {
    return this.me$.pipe(hasDeleteAccessTokenPermission(accessToken));
  }

  onDelete(accessToken: AccessToken) {
    this.facade.deleteAccessToken(accessToken);
  }

  allowDeleteAll$(accessTokens: AccessToken[]): Observable<boolean> {
    return this.me$.pipe(hasDeleteAllAccessTokensPermission(accessTokens));
  }

  onDeleteAll(accessTokens: AccessToken[]) {
    this.facade.deleteAccessTokens(accessTokens);
  }

  ngOnDestroy(): void {
    this.facade.unloadAccessTokens();
  }
}
