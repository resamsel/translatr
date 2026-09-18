import { Injector } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterTestingModule } from '@angular/router/testing';
import { ShortNumberPipe } from '@dev/translatr-components';
import { MockMetricComponent, MockUserCardComponent } from '@translatr/components/testing';
import { mockObservable } from '@translatr/utils/testing';
import { TimeAgoPipe } from '@dev/translatr-components';
import { UserFacade } from '../+state/user.facade';
import { MockActivityListComponent } from '../../../shared/activity-list/testing';
import { MockProjectCardListComponent } from '../../../shared/project-card-list/testing';
import { USER_ROUTES } from '../user-page.token';

import { UserInfoComponent } from './user-info.component';

describe('UserInfoComponent', () => {
  let component: UserInfoComponent;
  let fixture: ComponentFixture<UserInfoComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        declarations: [UserInfoComponent],
        imports: [
          MockUserCardComponent,
          MockMetricComponent,
          ShortNumberPipe,
          MockProjectCardListComponent,
          MockActivityListComponent,

          RouterTestingModule,
          TimeAgoPipe,

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
