import { Injector } from '@angular/core';
import { AccessToken, Scope, User } from '@dev/translatr-model';
import {
  AccessTokenService,
  ActivityService,
  ProjectService,
  UserService
} from '@dev/translatr-sdk';
import { Observable, of } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';
import { chooseAccessToken } from '../access-token';
import { LoadGeneratorConfig } from '../load-generator-config';
import { selectRandomUserAccessToken } from '../user';
import { WeightedPersona } from '../weighted-persona';
import { Persona } from './persona';
import { personas } from './personas';

const info: WeightedPersona = {
  section: 'user',
  type: 'read',
  name: 'Janine',
  description: 'I\'m going to peek at myself (a random user).',
  weight: 100
};

export class JaninePersona extends Persona {
  private readonly userService: UserService;
  private readonly accessTokenService: AccessTokenService;
  private readonly activityService: ActivityService;
  private readonly projectService: ProjectService;

  constructor(config: LoadGeneratorConfig, injector: Injector) {
    super(info.name, config, injector);

    this.userService = injector.get(UserService);
    this.accessTokenService = injector.get(AccessTokenService);
    this.activityService = injector.get(ActivityService);
    this.projectService = injector.get(ProjectService);
  }

  execute(): Observable<string> {
    return selectRandomUserAccessToken(this.accessTokenService, this.userService).pipe(
      switchMap((result: { user: User; accessToken: AccessToken }) =>
        this.projectService
          .find({
            access_token: chooseAccessToken(
              result.accessToken,
              this.config.accessToken,
              Scope.ProjectRead
            ),
            ownerId: result.user.id,
            fetch: 'count'
          })
          .pipe(
            catchError(() => of({ total: -1 })),
            map(paged => ({ ...result, projectCount: paged.total }))
          )
      ),
      switchMap((result: { user: User; accessToken: AccessToken; projectCount: number }) =>
        this.accessTokenService
          .find({ access_token: result.accessToken.key, userId: result.user.id, fetch: 'count' })
          .pipe(
            catchError(() => of({ total: -1 })),
            map(paged => ({ ...result, accessTokenCount: paged.total }))
          )
      ),
      switchMap(
        (result: {
          user: User;
          accessToken: AccessToken;
          projectCount: number;
          accessTokenCount: number;
        }) =>
          this.activityService
            .find({ access_token: result.accessToken.key, userId: result.user.id, fetch: 'count' })
            .pipe(
              catchError(() => of({ total: -1 })),
              map(paged => ({ ...result, activityCount: paged.total }))
            )
      ),
      map(
        ({ user, projectCount, accessTokenCount, activityCount }) =>
          `user ${user.name} (${user.username}, ${projectCount}/${accessTokenCount}/${activityCount}) viewed`
      )
    );
  }
}

personas.push({
  ...info,
  create: (config: LoadGeneratorConfig, injector: Injector) => new JaninePersona(config, injector)
});
