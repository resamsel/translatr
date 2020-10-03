import { Injector } from '@angular/core';
import { LoadGeneratorConfig } from '../load-generator-config';
import { Observable } from 'rxjs';

export abstract class Persona {
  protected constructor(
    public readonly name: string,
    protected readonly config: LoadGeneratorConfig,
    protected readonly injector: Injector
  ) {}

  abstract execute(): Observable<string>;
}
