import { TestBed } from '@angular/core/testing';
import { Activity, PagedList } from '@dev/translatr-model';
import { ActivityService } from '@dev/translatr-sdk';
import { Actions } from '@ngrx/effects';
import { BehaviorSubject, of, Subject } from 'rxjs';
import { ActivitiesLoaded, LoadActivities } from './dashboard.actions';
import { DashboardEffects } from './dashboard.effects';

describe('DashboardEffects', () => {
  let actions: Subject<any>;
  let effects: DashboardEffects;
  let activityService: ActivityService & {
    me: jest.Mock;
    find: jest.Mock;
  };

  beforeEach(() => {
    actions = new BehaviorSubject(undefined);
    TestBed.configureTestingModule({
      providers: [
        DashboardEffects,
        {
          provide: ActivityService,
          useFactory: () => ({
            find: jest.fn()
          })
        },
        { provide: Actions, useValue: actions }
      ]
    });

    effects = TestBed.get(DashboardEffects);
    activityService = TestBed.get(ActivityService);
  });

  describe('loadActivities$', () => {
    it('should work', done => {
      // given
      const payload: PagedList<Activity> = {
        list: [],
        hasNext: false,
        hasPrev: false,
        limit: 20,
        offset: 0
      };
      activityService.find.mockReturnValueOnce(of(payload));
      const target$ = effects.loadActivities$;

      // when
      actions.next(new LoadActivities());

      // then
      target$.subscribe(actual => {
        expect(actual).toEqual(new ActivitiesLoaded(payload));
        expect(activityService.find.mock.calls.length).toEqual(1);
        done();
      });
    });
  });
});
