import { TestBed } from '@angular/core/testing';
import { BehaviorSubject, of, Subject } from 'rxjs';
import { Actions } from '@ngrx/effects';
import { DashboardEffects } from './dashboard.effects';
import { ActivityService } from '@dev/translatr-sdk';
import { Activity, PagedList } from '@dev/translatr-model';
import { ActivitiesLoaded, LoadActivities } from './dashboard.actions';

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
          provide: ActivityService, useFactory: () => ({
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
    it('should work', (done) => {
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
        expect(actual).toStrictEqual(new ActivitiesLoaded(payload));
        expect(activityService.find.mock.calls.length).toBe(1);
        done();
      });
    });
  });
});
