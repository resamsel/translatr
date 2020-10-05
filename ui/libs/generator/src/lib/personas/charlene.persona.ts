import { Injector } from '@angular/core';
import { UserService } from '@dev/translatr-sdk';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { LoadGeneratorConfig } from '../load-generator-config';
import { createRandomUser } from '../user';
import { Persona } from './persona';
import { personas } from './personas';

const name = 'Charlene';

/**
 * I'm going to create a user.
 */
export class CharlenePersona extends Persona {
  private readonly userService: UserService;

  constructor(config: LoadGeneratorConfig, injector: Injector) {
    super(name, config, injector);

    this.userService = injector.get(UserService);
  }

  execute(): Observable<string> {
    return createRandomUser(this.userService).pipe(map(({ message }) => message));
  }
}

personas.push({
  name,
  create: (config: LoadGeneratorConfig, injector: Injector) =>
    new CharlenePersona(config, injector),
  weight: 5
});
