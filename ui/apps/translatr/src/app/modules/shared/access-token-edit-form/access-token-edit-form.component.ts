import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Input } from '@angular/core';
import { AccessToken, Scope, scopes } from '@dev/translatr-model';
import { MatCheckboxChange } from '@angular/material/checkbox';
import { MatDialogRef, MatSnackBar } from '@angular/material';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { BaseEditFormComponent } from '../edit-form/base-edit-form-component';
import { UserFacade } from '../../pages/user-page/+state/user.facade';

const distinct = <T>(value: T, index: number, self: Array<T>) =>
  self.indexOf(value) === index;

const scopeType = scope => scope.split(':')[1];
const scopePermission = scope => scope.split(':')[0];

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-access-token-edit-form',
  templateUrl: './access-token-edit-form.component.html',
  styleUrls: ['./access-token-edit-form.component.scss']
})
export class AccessTokenEditFormComponent
  extends BaseEditFormComponent<AccessTokenEditFormComponent, AccessToken> {

  @Input() set accessToken(accessToken: AccessToken) {
    this.updateValue(accessToken);
  }

  @Input() dialogRef: MatDialogRef<any, AccessToken>;

  scopeTypes = scopes.map(scopeType).filter(distinct);
  scopeMap = scopes.reduce((acc, curr) => {
    const type = scopeType(curr);
    if (acc[type] === undefined) {
      acc[type] = [];
    }
    acc[type].push(curr);
    return acc;
  }, {});
  scopePermission = scopePermission;
  activeScopeMap = scopes.reduce((acc, curr) =>
    ({ ...acc, [curr]: false }), {});

  readonly nameFormControl = this.form.get('name');

  constructor(
    readonly snackBar: MatSnackBar,
    readonly facade: UserFacade,
    readonly changeDetectorRef: ChangeDetectorRef
  ) {
    super(
      snackBar,
      undefined,
      new FormGroup({
        id: new FormControl(''),
        name: new FormControl('', Validators.required),
        key: new FormControl({value: ''}),
        scope: new FormControl('')
      }),
      {key: '', name: '', scope: ''},
      (accessToken: AccessToken) => facade.createAccessToken(accessToken),
      (accessToken: AccessToken) => facade.updateAccessToken(accessToken),
      facade.accessTokenModified$,
      (accessToken: AccessToken) => `AccessToken ${accessToken.name} has been saved`,
      changeDetectorRef
    );
  }

  get dirty(): boolean {
    return this.form.dirty;
  }

  onChangeScope(scope: Scope, event: MatCheckboxChange) {
    this.activeScopeMap[scope] = event.checked;
    this.form.get('scope')
      .setValue(scopes.filter(s => this.activeScopeMap[s]).join(','));
    this.form.get('scope').markAsDirty();
  }

  private updateValue(accessToken: AccessToken): void {
    this.form.patchValue(accessToken);
    if (accessToken.scope !== undefined) {
      accessToken.scope
        .split(',')
        .forEach(scope => this.activeScopeMap[scope] = true);
    }
  }
}
