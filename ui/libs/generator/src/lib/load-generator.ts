import { Injector } from '@angular/core';
import { ErrorHandler, errorMessage } from '@dev/translatr-sdk';
import { cli } from 'cli-ux';
import { interval } from 'rxjs';
import { catchError, map, mergeMap, retryWhen, tap } from 'rxjs/operators';
import { createInjector } from './api';
import { LoadGeneratorConfig } from './load-generator-config';
import { Persona, personas } from './personas';
import { genericRetryStrategy, selectPersonaFactory } from './utils';
import { WeightedPersonaFactory } from './weighted-persona-factory';

const cutOffAfter = (secret: string, length: number): string => {
  return secret.substr(0, length) + (secret.length > length ? '...' : '');
};

export class LoadGenerator {
  private readonly config: LoadGeneratorConfig;
  private readonly injector: Injector;

  constructor(config: LoadGeneratorConfig) {
    this.config = { ...config };

    this.injector = createInjector(config.baseUrl, config.accessToken);
  }

  async execute() {
    cli.table(
      [
        { config: 'baseUrl', value: this.config.baseUrl },
        { config: 'accessToken', value: cutOffAfter(this.config.accessToken, 8) },
        { config: 'usersPerMinute', value: this.config.usersPerMinute },
        { config: 'personas', value: this.config.includePersonas },
        { config: 'maxRetryAttempts', value: this.config.maxRetryAttempts },
        { config: 'retryScalingDelay', value: this.config.retryScalingDelay }
      ],
      { config: {}, value: {} }
    );

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

    return interval((60 / this.config.usersPerMinute) * 1000)
      .pipe(
        map(() =>
          selectPersonaFactory(filteredPersonas, totalWeight).create(this.config, this.injector)
        ),
        mergeMap((persona: Persona) => {
          const startedMillis = new Date().getTime();
          cli.action.start(`Processing ${persona.name}`);
          return persona.execute().pipe(
            tap(message => {
              console.log(
                `${persona.name}: ${message} in ${new Date().getTime() - startedMillis}ms`
              );
            }),
            retryWhen(
              genericRetryStrategy({
                maxRetryAttempts: this.config.maxRetryAttempts,
                scalingDuration: this.config.retryScalingDelay,
                prefix: `${persona.name}: `
              })
            ),
            catchError(error => {
              console.error(
                `${persona.name}: ${errorMessage(error)} in ${new Date().getTime() -
                  startedMillis}ms`
              );
              return errorHandler.handleError(error);
            })
          );
        })
      )
      .toPromise();
  }
}
