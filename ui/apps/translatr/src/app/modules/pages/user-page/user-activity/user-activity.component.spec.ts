import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { ActivityGraphTestingModule } from '@translatr/components/testing';
import { mockObservable } from '@translatr/utils/testing';
import { UserFacade } from '../+state/user.facade';
import { ActivityListTestingModule } from '../../../shared/activity-list/testing';
import { UserActivityComponent } from './user-activity.component';

describe('UserActivityComponent', () => {
  let component: UserActivityComponent;
  let fixture: ComponentFixture<UserActivityComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        declarations: [UserActivityComponent],
        imports: [ActivityListTestingModule, ActivityGraphTestingModule],
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
