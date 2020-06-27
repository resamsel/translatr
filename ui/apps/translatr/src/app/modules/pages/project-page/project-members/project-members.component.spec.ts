import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { MatSnackBar } from '@angular/material/snack-bar';
import { RouterTestingModule } from '@angular/router/testing';
import { mockObservable } from '@translatr/utils/testing';
import { ProjectFacade } from '../../../shared/project-state';
import { AppFacade } from '../../../../+state/app.facade';
import { MemberListTestingModule } from './member-list/testing';

import { ProjectMembersComponent } from './project-members.component';

describe('ProjectMembersComponent', () => {
  let component: ProjectMembersComponent;
  let fixture: ComponentFixture<ProjectMembersComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ProjectMembersComponent],
      imports: [MemberListTestingModule, RouterTestingModule],
      providers: [
        {
          provide: ProjectFacade,
          useFactory: () => ({
            project$: mockObservable(),
            membersCriteria$: mockObservable(),
            members$: mockObservable(),
            unload$: mockObservable()
          })
        },
        {
          provide: AppFacade,
          useFactory: () => ({
            me$: mockObservable()
          })
        },
        { provide: MatSnackBar, useFactory: () => ({}) }
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectMembersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
