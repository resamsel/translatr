import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { TranslocoTestingModule } from '@ngneat/transloco';
import { mockObservable } from '@translatr/utils/testing';
import { ProjectFacade } from '../project-state/+state';
import { UsersFacade } from '../../pages/users-page/+state/users.facade';
import { ProjectOwnerEditFormTestingModule } from '../testing';
import { ProjectOwnerEditDialogComponent } from './project-owner-edit-dialog.component';

describe('ProjectMemberEditDialogComponent', () => {
  let component: ProjectOwnerEditDialogComponent;
  let fixture: ComponentFixture<ProjectOwnerEditDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ProjectOwnerEditDialogComponent],
      imports: [
        ProjectOwnerEditFormTestingModule,

        TranslocoTestingModule,

        MatDialogModule,
        MatButtonModule
      ],
      providers: [
        { provide: MatDialogRef, useValue: {} },
        { provide: MAT_DIALOG_DATA, useValue: {} },
        {
          provide: UsersFacade,
          useFactory: () => ({
            users$: mockObservable()
          })
        },
        {
          provide: ProjectFacade,
          useFactory: () => ({
            members$: mockObservable(),
            loadMembers: jest.fn()
          })
        }
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectOwnerEditDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
