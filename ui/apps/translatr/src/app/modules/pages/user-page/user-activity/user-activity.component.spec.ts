import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { UserActivityComponent } from './user-activity.component';
import { ActivityListTestingModule } from '../../../shared/activity-list/testing';
import { UserFacade } from '../+state/user.facade';
import { mockObservable } from '@translatr/utils/testing';

describe('UserActivityComponent', () => {
  let component: UserActivityComponent;
  let fixture: ComponentFixture<UserActivityComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [UserActivityComponent],
      imports: [ActivityListTestingModule],
      providers: [
        {
          provide: UserFacade,
          useFactory: () => ({
            criteria$: mockObservable(),
            user$: mockObservable(),
            destroy$: mockObservable()
          })
        }
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UserActivityComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
