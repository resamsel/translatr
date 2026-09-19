import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { ActivityGraphComponent } from '@dev/translatr-components';
import { MockActivityGraphComponent } from '@translatr/components/testing';
import { mockObservable } from '@translatr/utils/testing';
import { UserFacade } from '../+state/user.facade';
import { ActivityListComponent } from '../../../shared/activity-list/activity-list.component';
import { MockActivityListComponent } from '../../../shared/activity-list/testing';
import { UserActivityComponent } from './user-activity.component';

describe('UserActivityComponent', () => {
  let component: UserActivityComponent;
  let fixture: ComponentFixture<UserActivityComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.overrideComponent(UserActivityComponent, {
        remove: { imports: [ActivityListComponent, ActivityGraphComponent] },
        add: { imports: [MockActivityListComponent, MockActivityGraphComponent] }
      }).configureTestingModule({
        imports: [UserActivityComponent],
        providers: [
          {
            provide: UserFacade,
            useFactory: () => ({
              criteria$: mockObservable(),
              user$: mockObservable(),
              activityAggregated$: mockObservable(),
              destroy$: mockObservable()
            })
          }
        ]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(UserActivityComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
