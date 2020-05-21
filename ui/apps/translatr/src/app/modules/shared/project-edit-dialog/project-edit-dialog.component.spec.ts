import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ProjectEditDialogComponent } from './project-edit-dialog.component';
import { ReactiveFormsModule } from '@angular/forms';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef, MatFormFieldModule, MatInputModule, MatSnackBar } from '@angular/material';
import { AppFacade } from '../../../+state/app.facade';
import { TranslocoTestingModule } from '@ngneat/transloco';
import { mockObservable } from '@translatr/utils/testing';

describe('ProjectEditDialogComponent', () => {
  let component: ProjectEditDialogComponent;
  let fixture: ComponentFixture<ProjectEditDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ProjectEditDialogComponent],
      imports: [
        ReactiveFormsModule,
        NoopAnimationsModule,
        TranslocoTestingModule,

        MatDialogModule,
        MatFormFieldModule,
        MatInputModule
      ],
      providers: [
        { provide: MatSnackBar, useValue: {} },
        { provide: MatDialogRef, useValue: {} },
        { provide: MAT_DIALOG_DATA, useValue: { locale: {} } },
        {provide: AppFacade, useFactory: () => ({projectModified$: mockObservable()})}
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectEditDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
