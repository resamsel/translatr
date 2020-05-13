import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ProjectMemberEditDialogComponent } from './project-member-edit-dialog.component';
import { MAT_DIALOG_DATA, MatButtonModule, MatDialogModule, MatDialogRef } from '@angular/material';
import { UsersFacade } from '../../pages/users-page/+state/users.facade';
import { AppFacade } from '../../../+state/app.facade';
import { ProjectMemberEditFormTestingModule } from '../testing';
import { mockObservable } from '@translatr/utils/testing';
import { TranslocoTestingModule } from '@ngneat/transloco';

describe('ProjectMemberEditDialogComponent', () => {
  let component: ProjectMemberEditDialogComponent;
  let fixture: ComponentFixture<ProjectMemberEditDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ProjectMemberEditDialogComponent],
      imports: [
        ProjectMemberEditFormTestingModule,
        TranslocoTestingModule,

        MatDialogModule,
        MatButtonModule
      ],
      providers: [
        { provide: MatDialogRef, useValue: {} },
        { provide: MAT_DIALOG_DATA, useValue: { locale: {} } },
        {
          provide: UsersFacade,
          useFactory: () => ({
            users$: mockObservable()
          })
        },
        { provide: AppFacade, useFactory: () => ({}) }
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectMemberEditDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
