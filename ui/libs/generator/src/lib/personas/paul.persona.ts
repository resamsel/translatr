import { Injector } from '@angular/core';
import { ActivityService, UserService } from '@dev/translatr-sdk';
import { Observable } from 'rxjs';
import { map, switchMap } from 'rxjs/operators';
import { LoadGeneratorConfig } from '../load-generator-config';
import { WeightedPersona } from '../weighted-persona';
import { Persona } from './persona';
import { personas } from './personas';

const info: WeightedPersona = {
  section: 'user',
  type: 'read',
  name: 'Paul',
  description: 'I\'m going to peek at myself (the main user).',
  weight: 1
};

export class PaulPersona extends Persona {
  private readonly userService: UserService;
  private readonly activityService: ActivityService;

  constructor(config: LoadGeneratorConfig, injector: Injector) {
    super(info.name, config, injector);

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
      map(({ me, activities }) => `user ${me.name} (${me.username}) has ${activities} activities`)
    );
  }
}

personas.push({
  ...info,
  create: (config: LoadGeneratorConfig, injector: Injector) => new PaulPersona(config, injector)
});
