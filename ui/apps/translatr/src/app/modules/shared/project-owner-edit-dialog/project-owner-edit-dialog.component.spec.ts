import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { TranslocoTestingModule } from '@jsverse/transloco';
import { mockObservable } from '@translatr/utils/testing';
import { ProjectFacade } from '../project-state/+state';
import { UsersModule } from '../../pages/users-page/+state/users.module';
import { UsersFacade } from '../../pages/users-page/+state/users.facade';
import { ProjectMemberEditFormComponent } from '../project-member-edit-form/project-member-edit-form.component';
import { ProjectOwnerEditFormComponent } from '../project-owner-edit-form/project-owner-edit-form.component';
import { MockProjectMemberEditFormComponent, MockProjectOwnerEditFormComponent } from '../testing';
import { ProjectOwnerEditDialogComponent } from './project-owner-edit-dialog.component';

describe('ProjectMemberEditDialogComponent', () => {
  let component: ProjectOwnerEditDialogComponent;
  let fixture: ComponentFixture<ProjectOwnerEditDialogComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.overrideComponent(ProjectOwnerEditDialogComponent, {
        remove: { imports: [ProjectMemberEditFormComponent, ProjectOwnerEditFormComponent, UsersModule] },
        add: { imports: [MockProjectMemberEditFormComponent, MockProjectOwnerEditFormComponent] }
      }).configureTestingModule({
        imports: [
          ProjectOwnerEditDialogComponent,

          TranslocoTestingModule.forRoot({ langs: {}, translocoConfig: { availableLangs: ['en'] } }),

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
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectOwnerEditDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
