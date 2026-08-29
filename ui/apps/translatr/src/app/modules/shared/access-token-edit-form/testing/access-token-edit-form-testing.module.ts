import { Component, Input, NgModule, ChangeDetectionStrategy } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import { AccessToken } from '@dev/translatr-model';

@Component({
  standalone: false,
  selector: 'app-access-token-edit-form',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: ''
})
export class MockAccessTokenEditFormComponent {
  @Input() accessToken: AccessToken;
  @Input() dialogRef: MatDialogRef<any, AccessToken>;
}

@NgModule({
  declarations: [MockAccessTokenEditFormComponent],
  exports: [MockAccessTokenEditFormComponent]
})
export class AccessTokenEditFormTestingModule {}
