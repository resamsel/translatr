import { HttpErrorResponse } from '@angular/common/http';
import { Injector } from '@angular/core';
import { errorMessage, UserService } from '@dev/translatr-sdk';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
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
    return createRandomUser(this.userService).pipe(
      map(user => `user ${user.name} (${user.username}) created`),
      catchError((err: HttpErrorResponse) => of(`user could not be created (${errorMessage(err)})`))
    );
  }
}

personas.push({
  name,
  create: (config: LoadGeneratorConfig, injector: Injector) =>
    new CharlenePersona(config, injector),
  weight: 2
});
