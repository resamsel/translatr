import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ProjectMemberEditFormComponent } from './project-member-edit-form.component';
import { ReactiveFormsModule } from '@angular/forms';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ChangeDetectorRef } from '@angular/core';
import { ProjectFacade } from '../../pages/project-page/+state/project.facade';
import { TranslocoTestingModule } from '@ngneat/transloco';
import { mockObservable } from '@translatr/utils/testing';

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
        {provide: MatSnackBar, useValue: {}},
        {provide: ProjectFacade, useFactory: () => ({memberModified$: mockObservable()})},
        {provide: ChangeDetectorRef, useFactory: () => ({})},
        {provide: MAT_DIALOG_DATA, useValue: {}}
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
