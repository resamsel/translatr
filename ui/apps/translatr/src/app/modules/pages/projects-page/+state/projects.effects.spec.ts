import { TestBed } from '@angular/core/testing';
import { PagedList, Project } from '@dev/translatr-model';
import { ProjectService } from '@dev/translatr-sdk';
import { Actions } from '@ngrx/effects';
import { BehaviorSubject, of, Subject } from 'rxjs';
import { LoadProjects, ProjectsLoaded } from './projects.actions';
import { ProjectsEffects } from './projects.effects';

describe('ProjectsEffects', () => {
  let actions: Subject<any>;
  let projectService: ProjectService & {
    find: jest.Mock;
  };

  beforeEach(() => {
    actions = new BehaviorSubject(undefined);
    TestBed.configureTestingModule({
      providers: [
        ProjectsEffects,
        {
          provide: ProjectService,
          useFactory: () => ({
            find: jest.fn()
          })
        },
        { provide: Actions, useValue: actions }
      ]
    });

    projectService = TestBed.get(ProjectService);
  });

  describe('loadProjects$', () => {
    it('should work', done => {
      // given
      const payload: PagedList<Project> = {
        list: [],
        hasNext: false,
        hasPrev: false,
        limit: 20,
        offset: 0
      };
      const criteria = {};
      projectService.find.mockReturnValueOnce(of(payload));
      const effects: ProjectsEffects = TestBed.get(ProjectsEffects);
      const target$ = effects.loadProjects$;

      // when
      actions.next(new LoadProjects(criteria));

      // then
      target$.subscribe(actual => {
        expect(actual).toEqual(new ProjectsLoaded(payload));
        expect(projectService.find.mock.calls.length).toEqual(1);
        done();
      });
    });
  });
});
