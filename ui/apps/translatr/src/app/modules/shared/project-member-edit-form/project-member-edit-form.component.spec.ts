import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ProjectMemberEditFormComponent } from './project-member-edit-form.component';
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
import { ProjectFacade } from '../../pages/project-page/+state/project.facade';
import { TranslocoTestingModule } from '@ngneat/transloco';

describe('ProjectMemberEditFormComponent', () => {
  let component: ProjectMemberEditFormComponent;
  let fixture: ComponentFixture<ProjectMemberEditFormComponent>;

  beforeEach(async(() => {
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
        { provide: ProjectFacade, useFactory: () => ({}) },
        { provide: ChangeDetectorRef, useFactory: () => ({}) },
        { provide: MAT_DIALOG_DATA, useValue: {} }
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectMemberEditFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
