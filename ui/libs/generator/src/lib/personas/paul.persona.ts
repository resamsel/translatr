import { Injector } from '@angular/core';
import { ActivityService, UserService } from '@dev/translatr-sdk';
import { personas } from './personas';
import { Observable } from 'rxjs';
import { map, switchMap } from 'rxjs/operators';
import { LoadGeneratorConfig } from '../load-generator-config';
import { Persona } from './persona';

/**
 * I'm going to peek at myself (the main user).
 */
export class PaulPersona extends Persona {
  private readonly userService: UserService;
  private readonly activityService: ActivityService;

  constructor(config: LoadGeneratorConfig, injector: Injector) {
    super('Paul', config, injector);

    this.userService = injector.get(UserService);
    this.activityService = injector.get(ActivityService);
  }

  execute(): Observable<string> {
    return this.userService.me().pipe(
      switchMap(me =>
        this.activityService
          .find({ userId: me.id, fetch: 'count' })
          .pipe(map(paged => ({ me, activities: paged.total })))
      ),
      map(
        ({ me, activities }) =>
          `My name is ${me.name} (${me.username}) with ${activities} activities`
      )
    );
  }
}

personas.push({
  create: (config: LoadGeneratorConfig, injector: Injector) => new PaulPersona(config, injector),
  weight: 1
});
