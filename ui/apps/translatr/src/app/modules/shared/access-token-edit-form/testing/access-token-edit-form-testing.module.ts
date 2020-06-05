import { Component, Input, NgModule } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import { AccessToken } from '@dev/translatr-model';

@Component({
  selector: 'app-access-token-edit-form',
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
