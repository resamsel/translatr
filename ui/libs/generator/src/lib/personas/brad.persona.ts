import { Injector } from '@angular/core';
import { UserService } from '@dev/translatr-sdk';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { LoadGeneratorConfig } from '../load-generator-config';
import { deleteRandomUser } from '../user';
import { Persona } from './persona';
import { personas } from './personas';

/**
 * I'm going to delete a user.
 */
export class BradPersona extends Persona {
  private readonly userService: UserService;

  constructor(config: LoadGeneratorConfig, injector: Injector) {
    super('Brad', config, injector);

    this.userService = injector.get(UserService);
  }

  execute(): Observable<string> {
    return deleteRandomUser(this.userService).pipe(map(({ message }) => message));
  }
}

personas.push({
  create: (config: LoadGeneratorConfig, injector: Injector) => new BradPersona(config, injector),
  weight: 1
});
