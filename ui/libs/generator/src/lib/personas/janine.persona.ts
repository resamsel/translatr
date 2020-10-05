import { Injector } from '@angular/core';
import { AccessToken, User } from '@dev/translatr-model';
import {
  AccessTokenService,
  ActivityService,
  ProjectService,
  UserService
} from '@dev/translatr-sdk';
import { selectRandomUser } from '../user';
import { Observable, of } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';
import { LoadGeneratorConfig } from '../load-generator-config';
import { Persona } from './persona';
import { personas } from './personas';

/**
 * I'm going to peek at myself (a random user).
 */
export class JaninePersona extends Persona {
  private readonly userService: UserService;
  private readonly accessTokenService: AccessTokenService;
  private readonly activityService: ActivityService;
  private readonly projectService: ProjectService;

  constructor(config: LoadGeneratorConfig, injector: Injector) {
    super('Janine', config, injector);

    this.userService = injector.get(UserService);
    this.accessTokenService = injector.get(AccessTokenService);
    this.activityService = injector.get(ActivityService);
    this.projectService = injector.get(ProjectService);
  }

  execute(): Observable<string> {
    return selectRandomUser(this.accessTokenService, this.userService).pipe(
      switchMap((result: { user: User; accessToken: AccessToken }) =>
        this.projectService
          .find({ access_token: result.accessToken.key, ownerId: result.user.id, fetch: 'count' })
          .pipe(
            catchError(error => of({ total: -1 })),
            map(paged => ({ ...result, projectCount: paged.total }))
          )
      ),
      switchMap((result: { user: User; accessToken: AccessToken; projectCount: number }) =>
        this.accessTokenService
          .find({ access_token: result.accessToken.key, userId: result.user.id, fetch: 'count' })
          .pipe(
            catchError(error => of({ total: -1 })),
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
              catchError(error => of({ total: -1 })),
              map(paged => ({ ...result, activityCount: paged.total }))
            )
      ),
      map(
        ({ user, projectCount, accessTokenCount, activityCount }) =>
          `My name is ${user.name} (${user.username}) - ${projectCount} projects, ${accessTokenCount} access tokens, ${activityCount} activities`
      )
    );
  }
}

personas.push({
  create: (config: LoadGeneratorConfig, injector: Injector) => new JaninePersona(config, injector),
  weight: 20
});
