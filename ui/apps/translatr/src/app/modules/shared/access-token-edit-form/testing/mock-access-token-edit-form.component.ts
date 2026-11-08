import { Component, Input, ChangeDetectionStrategy } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import { AccessToken } from '@dev/translatr-model';

@Component({
  standalone: true,
  selector: 'app-access-token-edit-form',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: ''
})
export class MockAccessTokenEditFormComponent {
  @Input() accessToken: AccessToken;
  @Input() dialogRef: MatDialogRef<any, AccessToken>;
}
