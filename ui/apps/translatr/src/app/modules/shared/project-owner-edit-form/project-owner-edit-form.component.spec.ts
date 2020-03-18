import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ProjectOwnerEditFormComponent } from './project-owner-edit-form.component';
import { ReactiveFormsModule } from '@angular/forms';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import {
  MAT_DIALOG_DATA,
  MatAutocompleteModule,
  MatFormFieldModule,
  MatInputModule,
  MatSelectModule,
  MatSnackBar
} from '@angular/material';
import { ChangeDetectorRef } from '@angular/core';
import { ProjectService } from '@dev/translatr-sdk';

describe('ProjectMemberEditFormComponent', () => {
  let component: ProjectOwnerEditFormComponent;
  let fixture: ComponentFixture<ProjectOwnerEditFormComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ProjectOwnerEditFormComponent],
      imports: [
        ReactiveFormsModule,
        NoopAnimationsModule,

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
