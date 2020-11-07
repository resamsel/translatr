import { HttpErrorResponse } from '@angular/common/http';
import { Injector } from '@angular/core';
import { errorMessage, UserService } from '@dev/translatr-sdk';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { LoadGeneratorConfig } from '../load-generator-config';
import { createRandomUser } from '../user';
import { WeightedPersona } from '../weighted-persona';
import { Persona } from './persona';
import { personas } from './personas';

const info: WeightedPersona = {
  section: 'user',
  type: 'create',
  name: 'Charlene',
  description: 'I\'m going to create a user.',
  weight: 2
};

export class CharlenePersona extends Persona {
  private readonly userService: UserService;

  constructor(config: LoadGeneratorConfig, injector: Injector) {
    super(info.name, config, injector);

    this.userService = injector.get(UserService);
  }

  execute(): Observable<string> {
    return createRandomUser(this.userService).pipe(
      map(user => `user ${user.name} (${user.username}) created`),
      catchError((err: HttpErrorResponse) => of(`user could not be created (${errorMessage(err)})`))
    );
  }
}

personas.push({
  ...info,
  create: (config: LoadGeneratorConfig, injector: Injector) =>
    new CharlenePersona(config, injector)
});
