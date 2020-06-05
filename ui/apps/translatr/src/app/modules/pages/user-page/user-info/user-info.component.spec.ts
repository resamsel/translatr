import { Injector } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterTestingModule } from '@angular/router/testing';
import { ShortNumberModule } from '@dev/translatr-components';
import { MetricTestingModule, UserCardTestingModule } from '@translatr/components/testing';
import { mockObservable } from '@translatr/utils/testing';
import { MomentModule } from 'ngx-moment';
import { UserFacade } from '../+state/user.facade';
import { ActivityListTestingModule } from '../../../shared/activity-list/testing';
import { ProjectCardListTestingModule } from '../../../shared/project-card-list/testing';
import { USER_ROUTES } from '../user-page.token';

import { UserInfoComponent } from './user-info.component';

describe('UserInfoComponent', () => {
  let component: UserInfoComponent;
  let fixture: ComponentFixture<UserInfoComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [UserInfoComponent],
      imports: [
        UserCardTestingModule,
        MetricTestingModule,
        ShortNumberModule,
        ProjectCardListTestingModule,
        ActivityListTestingModule,

        RouterTestingModule,
        MomentModule,

        MatIconModule,
        MatTooltipModule
      ],
      providers: [
        { provide: Injector, useFactory: () => ({}) },
        {
          provide: UserFacade,
          useFactory: () => ({
            user$: mockObservable(),
            projects$: mockObservable(),
            activities$: mockObservable(),
            destroy$: mockObservable()
          })
        },
        { provide: MatDialog, useFactory: () => ({}) },
        {
          provide: USER_ROUTES,
          useValue: [{ children: [] }]
        }
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UserInfoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
