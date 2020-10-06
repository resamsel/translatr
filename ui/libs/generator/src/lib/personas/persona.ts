import { Injector } from '@angular/core';
import { Observable } from 'rxjs';
import { LoadGeneratorConfig } from '../load-generator-config';

export abstract class Persona {
  protected constructor(
    public readonly name: string,
    protected readonly config: LoadGeneratorConfig,
    protected readonly injector: Injector
  ) {}

  abstract execute(): Observable<string>;
}
