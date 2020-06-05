import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { TranslocoTestingModule } from '@ngneat/transloco';
import { mockObservable } from '@translatr/utils/testing';
import { AppFacade } from '../../../+state/app.facade';
import { UsersFacade } from '../../pages/users-page/+state/users.facade';
import { ProjectMemberEditFormTestingModule } from '../testing';

import { ProjectMemberEditDialogComponent } from './project-member-edit-dialog.component';

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
    }).compileComponents();
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
