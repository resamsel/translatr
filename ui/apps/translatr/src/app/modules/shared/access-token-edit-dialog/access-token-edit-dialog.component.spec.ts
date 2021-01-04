import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { AccessTokenEditDialogComponent } from './access-token-edit-dialog.component';
import { AccessTokenEditFormTestingModule } from '../access-token-edit-form/testing';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { TranslocoTestingModule } from '@ngneat/transloco';

describe('ProjectCreationDialogComponent', () => {
  let component: AccessTokenEditDialogComponent;
  let fixture: ComponentFixture<AccessTokenEditDialogComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        declarations: [AccessTokenEditDialogComponent],
        imports: [
          AccessTokenEditFormTestingModule,
          TranslocoTestingModule,

          MatDialogModule,
          MatButtonModule
        ],
        providers: [
          { provide: MatDialogRef, useFactory: () => ({}) },
          { provide: MAT_DIALOG_DATA, useValue: {} }
        ]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(AccessTokenEditDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
