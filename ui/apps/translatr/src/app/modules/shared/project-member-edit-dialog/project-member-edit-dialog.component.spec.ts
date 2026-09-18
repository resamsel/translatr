import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { TranslocoTestingModule } from '@jsverse/transloco';
import { mockObservable } from '@translatr/utils/testing';
import { AppFacade } from '../../../+state/app.facade';
import { UsersModule } from '../../pages/users-page/+state/users.module';
import { UsersFacade } from '../../pages/users-page/+state/users.facade';
import { ProjectMemberEditFormComponent } from '../project-member-edit-form/project-member-edit-form.component';
import { ProjectMemberEditFormTestingModule } from '../testing';

import { ProjectMemberEditDialogComponent } from './project-member-edit-dialog.component';

describe('ProjectMemberEditDialogComponent', () => {
  let component: ProjectMemberEditDialogComponent;
  let fixture: ComponentFixture<ProjectMemberEditDialogComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.overrideComponent(ProjectMemberEditDialogComponent, {
        remove: { imports: [ProjectMemberEditFormComponent, UsersModule] },
        add: { imports: [ProjectMemberEditFormTestingModule] }
      }).configureTestingModule({
        imports: [
          ProjectMemberEditDialogComponent,
          TranslocoTestingModule.forRoot({ langs: {}, translocoConfig: { availableLangs: ['en'] } }),

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
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectMemberEditDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
