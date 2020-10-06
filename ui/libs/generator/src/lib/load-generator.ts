import { Injector } from '@angular/core';
import { ErrorHandler } from '@dev/translatr-sdk';
import { cli } from 'cli-ux';
import { interval } from 'rxjs';
import { catchError, concatMap, map, tap } from 'rxjs/operators';
import { createInjector } from './api';
import { LoadGeneratorConfig } from './load-generator-config';
import { Persona, personas } from './personas';
import { selectPersonaFactory } from './utils';
import { WeightedPersonaFactory } from './weighted-persona-factory';

export class LoadGenerator {
  private readonly config: LoadGeneratorConfig;
  private readonly injector: Injector;

  constructor(config: LoadGeneratorConfig) {
    this.config = { ...config };

    this.injector = createInjector(config.baseUrl, config.accessToken);
  }

  async execute() {
    const filteredPersonas =
      this.config.includePersonas.length > 0
        ? personas.filter(persona => this.config.includePersonas.includes(persona.name))
        : personas;

    cli.table(filteredPersonas, { name: { header: 'Persona' }, weight: {} });

    const totalWeight = filteredPersonas.reduce(
      (agg: number, curr: WeightedPersonaFactory) => agg + curr.weight,
      0
    );

    const errorHandler = this.injector.get(ErrorHandler);

    console.log('\nGenerating load...\n');

    return interval((60 / this.config.requestsPerMinute) * 1000)
      .pipe(
        map(() =>
          selectPersonaFactory(filteredPersonas, totalWeight).create(this.config, this.injector)
        ),
        concatMap((persona: Persona) => {
          cli.action.start(`${persona.name}`, 'processing');
          return persona.execute().pipe(
            tap(message => cli.action.stop(message)),
            catchError(error => {
              cli.action.stop('failed');
              return errorHandler.handleError(error);
            })
          );
        })
      )
      .toPromise();
  }
}
