import { HttpErrorResponse } from '@angular/common/http';
import { Injector } from '@angular/core';
import { errorMessage, UserService } from '@dev/translatr-sdk';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { LoadGeneratorConfig } from '../load-generator-config';
import { deleteRandomUser } from '../user';
import { Persona } from './persona';
import { personas } from './personas';

const name = 'Brad';

/**
 * I'm going to delete a user.
 */
export class BradPersona extends Persona {
  private readonly userService: UserService;

  constructor(config: LoadGeneratorConfig, injector: Injector) {
    super(name, config, injector);

    this.userService = injector.get(UserService);
  }

  execute(): Observable<string> {
    return deleteRandomUser(this.userService).pipe(
      map(user => `user ${user.name} (${user.username}) deleted`),
      catchError((err: HttpErrorResponse) => of(`user could not be deleted (${errorMessage(err)})`))
    );
  }
}

personas.push({
  name,
  create: (config: LoadGeneratorConfig, injector: Injector) => new BradPersona(config, injector),
  weight: 1
});
