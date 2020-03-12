import { TestBed } from '@angular/core/testing';
import { BehaviorSubject, of, Subject } from 'rxjs';
import { AppEffects } from './app.effects';
import {
  createProject,
  loadMe,
  loadProject,
  loadUsers,
  meLoaded,
  projectCreated,
  projectLoaded,
  projectUpdated,
  updateProject,
  usersLoaded
} from './app.actions';
import { PagedList, Project, User } from '@dev/translatr-model';
import { ProjectService, UserService } from '@dev/translatr-sdk';
import { Actions } from '@ngrx/effects';

describe('AppEffects', () => {
  let actions: Subject<any>;
  let effects: AppEffects;
  let userService: UserService & {
    me: jest.Mock;
    find: jest.Mock;
  };
  let projectService: ProjectService & {
    byOwnerAndName: jest.Mock;
    create: jest.Mock;
    update: jest.Mock;
  };

  beforeEach(() => {
    actions = new BehaviorSubject(undefined);
    TestBed.configureTestingModule({
      providers: [
        AppEffects,
        {
          provide: UserService, useFactory: () => ({
            me: jest.fn(),
            find: jest.fn()
          })
        },
        {
          provide: ProjectService, useFactory: () => ({
            byOwnerAndName: jest.fn(),
            create: jest.fn(),
            update: jest.fn()
          })
        },
        { provide: Actions, useValue: actions }
      ]
    });

    effects = TestBed.get(AppEffects);
    userService = TestBed.get(UserService);
    projectService = TestBed.get(ProjectService);
  });

  describe('loadMe$', () => {
    it('should work', (done) => {
      // given
      const user: User = { id: '1', name: 'user', username: 'username' };
      userService.me.mockReturnValueOnce(of(user));
      const target$ = effects.loadMe$;

      // when
      actions.next(loadMe());

      // then
      target$.subscribe(actual => {
        expect(actual).toStrictEqual(meLoaded({ payload: user }));
        expect(userService.me.mock.calls.length).toBe(1);
        done();
      });
    });
  });

  describe('loadUsers$', () => {
    it('should work', (done) => {
      // given
      const payload: PagedList<User> = {
        list: [{ id: '1', name: 'user', username: 'username' }],
        hasNext: false,
        hasPrev: false,
        limit: 20,
        offset: 0
      };
      userService.find.mockReturnValueOnce(of(payload));
      const target$ = effects.loadUsers$;

      // when
      actions.next(loadUsers({ payload: {} }));

      // then
      target$.subscribe(actual => {
        expect(actual).toStrictEqual(usersLoaded({ payload }));
        expect(userService.find.mock.calls.length).toBe(1);
        done();
      });
    });
  });

  describe('loadProject$', () => {
    it('should work', (done) => {
      // given
      const payload: Project = { name: 'A' };
      projectService.byOwnerAndName.mockReturnValueOnce(of(payload));
      const target$ = effects.loadProject$;

      // when
      actions.next(loadProject({ payload: { username: 'a', projectName: 'A' } }));

      // then
      target$.subscribe(actual => {
        expect(actual).toStrictEqual(projectLoaded({ payload }));
        expect(projectService.byOwnerAndName.mock.calls.length).toBe(1);
        done();
      });
    });
  });

  describe('createProject$', () => {
    it('should work', (done) => {
      // given
      const payload: Project = { name: 'A' };
      projectService.create.mockReturnValueOnce(of(payload));
      const target$ = effects.createProject$;

      // when
      actions.next(createProject({ payload }));

      // then
      target$.subscribe(actual => {
        expect(actual).toStrictEqual(projectCreated({ payload }));
        expect(projectService.create.mock.calls.length).toBe(1);
        done();
      });
    });
  });

  describe('updateProject$', () => {
    it('should work', (done) => {
      // given
      const payload: Project = { name: 'A' };
      projectService.update.mockReturnValueOnce(of(payload));
      const target$ = effects.updateProject$;

      // when
      actions.next(updateProject({ payload }));

      // then
      target$.subscribe(actual => {
        expect(actual).toStrictEqual(projectUpdated({ payload }));
        expect(projectService.update.mock.calls.length).toBe(1);
        done();
      });
    });
  });
});
