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
import { TranslocoTestingModule } from '@ngneat/transloco';
import { mockObservable } from '@translatr/utils/testing';
import { ProjectFacade } from '../project-state/+state';

import { ProjectMemberEditFormComponent } from './project-member-edit-form.component';

describe('ProjectMemberEditFormComponent', () => {
  let component: ProjectMemberEditFormComponent;
  let fixture: ComponentFixture<ProjectMemberEditFormComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        declarations: [ProjectMemberEditFormComponent],
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
          { provide: ProjectFacade, useFactory: () => ({ memberModified$: mockObservable() }) },
          { provide: ChangeDetectorRef, useFactory: () => ({}) },
          { provide: MAT_DIALOG_DATA, useValue: {} }
        ]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectMemberEditFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
