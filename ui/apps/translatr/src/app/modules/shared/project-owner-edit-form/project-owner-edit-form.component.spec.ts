import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ProjectOwnerEditFormComponent } from './project-owner-edit-form.component';
import { ReactiveFormsModule } from '@angular/forms';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ChangeDetectorRef } from '@angular/core';
import { ProjectService } from '@dev/translatr-sdk';
import { TranslocoTestingModule } from '@ngneat/transloco';

describe('ProjectMemberEditFormComponent', () => {
  let component: ProjectOwnerEditFormComponent;
  let fixture: ComponentFixture<ProjectOwnerEditFormComponent>;

  beforeEach(async(() => {
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
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectOwnerEditFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
