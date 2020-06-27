import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { UserProjectsComponent } from './user-projects.component';
import { UserFacade } from '../+state/user.facade';
import { mockObservable } from '@translatr/utils/testing';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ProjectListTestingModule } from '../../../shared/project-list/testing';
import { RouterTestingModule } from '@angular/router/testing';

describe('UserProjectsComponent', () => {
  let component: UserProjectsComponent;
  let fixture: ComponentFixture<UserProjectsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [UserProjectsComponent],
      imports: [
        ProjectListTestingModule,

        RouterTestingModule,

        MatTooltipModule,
        MatButtonModule,
        MatIconModule
      ],
      providers: [
        {
          provide: UserFacade,
          useFactory: () => ({
            projectsCriteria$: mockObservable(),
            user$: mockObservable(),
            destroy$: mockObservable()
          })
        },
        { provide: MatDialog, useFactory: () => ({}) }
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UserProjectsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
