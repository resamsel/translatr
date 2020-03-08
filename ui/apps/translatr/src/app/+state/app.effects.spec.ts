import { TestBed } from '@angular/core/testing';
import { Observable, of } from 'rxjs';
import { provideMockActions } from '@ngrx/effects/testing';
import { AppEffects } from './app.effects';
import { loadMe, meLoaded } from './app.actions';
import { User } from '@dev/translatr-model';
import { ProjectService, UserService } from '@dev/translatr-sdk';

describe('AppEffects', () => {
  let actions: Observable<any>;
  let effects: AppEffects;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AppEffects,
        { provide: UserService, useFactory: () => ({ me: jest.fn() }) },
        { provide: ProjectService, useFactory: () => ({}) },
        provideMockActions(() => actions)
      ]
    });

    effects = TestBed.get(AppEffects);
  });

  describe('loadMe$', () => {
    it('should work', (done) => {
      // given
      actions = of(loadMe());
      const user: User = { id: '1', name: 'user', username: 'username' };
      const userService = TestBed.get(UserService);
      userService.me.mockReturnValueOnce(of(user));

      // when
      const actual$ = effects.loadMe$;

      // then
      actual$.subscribe(actual => {
        expect(actual).toStrictEqual(meLoaded({ payload: user }));
        done();
      });
    });
  });
});
