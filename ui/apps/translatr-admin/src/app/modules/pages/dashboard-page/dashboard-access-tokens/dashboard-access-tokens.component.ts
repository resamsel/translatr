import { ChangeDetectionStrategy, Component } from '@angular/core';
import { merge, Observable, of } from 'rxjs';
import { AccessToken, Feature, RequestCriteria } from '@dev/translatr-model';
import { AppFacade } from '../../../../+state/app.facade';
import { Entity, notifyEvent } from '@dev/translatr-components';
import {
  errorMessage,
  hasDeleteAccessTokenPermission,
  hasDeleteAllAccessTokensPermission,
  hasEditAccessTokenPermission
} from '@dev/translatr-sdk';
import {
  AccessTokenDeleted,
  AccessTokenDeleteError,
  AccessTokensDeleted,
  AccessTokensDeleteError,
  AppActionTypes
} from '../../../../+state/app.actions';
import { MatSnackBar } from '@angular/material/snack-bar';
import { mapTo } from 'rxjs/operators';
import { ofType } from '@ngrx/effects';
import { environment } from '../../../../../environments/environment';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'dev-dashboard-access-tokens',
  templateUrl: './dashboard-access-tokens.component.html',
  styleUrls: ['./dashboard-access-tokens.component.scss']
})
export class DashboardAccessTokensComponent {
  displayedColumns = ['name', 'user', 'scopes', 'when_created', 'actions'];

  me$ = this.facade.me$;
  accessTokens$ = this.facade.accessTokens$;
  load$ = merge(
    of({ limit: '20', order: 'whenCreated desc' }),
    this.facade.accessTokenDeleted$.pipe(
      ofType(AppActionTypes.AccessTokenDeleted),
      mapTo({})
    ),
    this.facade.accessTokensDeleted$.pipe(
      ofType(AppActionTypes.AccessTokensDeleted),
      mapTo({})
    )
  );

  selected: AccessToken[] = [];

  readonly uiUrl = environment.uiUrl;

  readonly Feature = Feature;

  constructor(
    private readonly facade: AppFacade,
    readonly snackBar: MatSnackBar
  ) {
    notifyEvent(
      snackBar,
      facade.accessTokenDeleted$,
      AppActionTypes.AccessTokenDeleted,
      (action: AccessTokenDeleted) =>
        `Access token ${action.payload.name} has been deleted`,
      (action: AccessTokenDeleteError) =>
        `Access token could not be deleted: ${errorMessage(action.payload)}`
    );
    notifyEvent(
      snackBar,
      facade.accessTokensDeleted$,
      AppActionTypes.AccessTokensDeleted,
      (action: AccessTokensDeleted) =>
        `${action.payload.length} access tokens have been deleted`,
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
    console.log('edit', accessToken);
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
}
