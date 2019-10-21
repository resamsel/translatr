import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnDestroy, OnInit } from '@angular/core';
import { AbstractEditFormComponent } from '../edit-form/abstract-edit-form-component';
import { AccessToken, Scope, scopes } from '@dev/translatr-model';
import { MatCheckboxChange } from '@angular/material/checkbox';
import { MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AccessTokenService } from '@dev/translatr-sdk';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { takeUntil } from 'rxjs/operators';
import { Subject } from 'rxjs';

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
  extends AbstractEditFormComponent<AccessTokenEditFormComponent, AccessToken>
  implements OnInit, OnDestroy {

  private destroy$ = new Subject<void>();

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
  activeScopeMap = scopes.reduce((acc, curr) => ({ ...acc, [curr]: false }), {});

  constructor(
    readonly snackBar: MatSnackBar,
    readonly accessTokenService: AccessTokenService,
    readonly changeDetectorRef: ChangeDetectorRef
  ) {
    super(
      snackBar,
      undefined,
      new FormGroup({
        id: new FormControl(''),
        name: new FormControl('', Validators.required),
        key: new FormControl({ value: '' }),
        scope: new FormControl('')
      }),
      { key: '', name: '', scope: '' },
      (accessToken: AccessToken) => accessTokenService.create(accessToken),
      (accessToken: AccessToken) => accessTokenService.update(accessToken),
      (accessToken: AccessToken) => `AccessToken ${accessToken.name} has been saved`
    );

  }

  ngOnInit(): void {
    this.failure.pipe(takeUntil(this.destroy$))
      .subscribe(() => this.changeDetectorRef.markForCheck());
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  get dirty(): boolean {
    return this.form.dirty;
  }

  public get nameFormControl() {
    return this.form.get('name');
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
