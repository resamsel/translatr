import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AccessTokenEditDialogComponent } from './access-token-edit-dialog.component';
import { AccessTokenEditFormTestingModule } from '../access-token-edit-form/testing';
import { MAT_DIALOG_DATA, MatButtonModule, MatDialogModule, MatDialogRef } from '@angular/material';
import { TranslocoTestingModule } from '@ngneat/transloco';

describe('ProjectCreationDialogComponent', () => {
  let component: AccessTokenEditDialogComponent;
  let fixture: ComponentFixture<AccessTokenEditDialogComponent>;

  beforeEach(async(() => {
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
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AccessTokenEditDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
