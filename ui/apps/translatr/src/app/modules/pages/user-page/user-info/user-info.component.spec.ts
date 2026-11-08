import { Injector } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { TranslocoTestingModule } from '@jsverse/transloco';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterTestingModule } from '@angular/router/testing';
import { MetricComponent, ShortNumberPipe, UserCardComponent } from '@dev/translatr-components';
import { MockMetricComponent, MockUserCardComponent } from '@translatr/components/testing';
import { mockObservable } from '@translatr/utils/testing';
import { TimeAgoPipe } from '@dev/translatr-components';
import { UserFacade } from '../+state/user.facade';
import { ActivityListComponent } from '../../../shared/activity-list/activity-list.component';
import { MockActivityListComponent } from '../../../shared/activity-list/testing';
import { ProjectCardListComponent } from '../../../shared/project-card-list/project-card-list.component';
import { MockProjectCardListComponent } from '../../../shared/project-card-list/testing';
import { USER_ROUTES } from '../user-page.token';

import { UserInfoComponent } from './user-info.component';

describe('UserInfoComponent', () => {
  let component: UserInfoComponent;
  let fixture: ComponentFixture<UserInfoComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.overrideComponent(UserInfoComponent, {
        remove: { imports: [UserCardComponent, MetricComponent, ActivityListComponent, ProjectCardListComponent] },
        add: { imports: [MockUserCardComponent, MockMetricComponent, MockProjectCardListComponent, MockActivityListComponent] }
      }).configureTestingModule({
        imports: [
          UserInfoComponent,
          ShortNumberPipe,

          RouterTestingModule,
          TimeAgoPipe,

          MatIconModule,
          MatTooltipModule,
          TranslocoTestingModule.forRoot({ langs: {}, translocoConfig: { availableLangs: ['en'] } })
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
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(UserInfoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
