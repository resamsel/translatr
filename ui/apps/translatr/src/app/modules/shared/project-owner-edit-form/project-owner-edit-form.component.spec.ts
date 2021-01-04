import { ChangeDetectorRef } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ProjectService } from '@dev/translatr-sdk';
import { TranslocoTestingModule } from '@ngneat/transloco';

import { ProjectOwnerEditFormComponent } from './project-owner-edit-form.component';

describe('ProjectMemberEditFormComponent', () => {
  let component: ProjectOwnerEditFormComponent;
  let fixture: ComponentFixture<ProjectOwnerEditFormComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        declarations: [ProjectOwnerEditFormComponent],
        imports: [
          ReactiveFormsModule,
          NoopAnimationsModule,
          TranslocoTestingModule,

          MatFormFieldModule,
          MatInputModule,
          MatSelectModule,
          MatAutocompleteModule
        ],
        providers: [
          { provide: MatSnackBar, useValue: {} },
          { provide: ProjectService, useFactory: () => ({}) },
          { provide: ChangeDetectorRef, useFactory: () => ({}) },
          { provide: MAT_DIALOG_DATA, useValue: {} }
        ]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectOwnerEditFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
