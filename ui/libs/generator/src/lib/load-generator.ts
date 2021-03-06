import { Injector } from '@angular/core';
import { ErrorHandler, errorMessage } from '@dev/translatr-sdk';
import { capitalize, capitalizeWords, cutOffAfter, groupBy } from '@translatr/utils';
import { cli } from 'cli-ux';
import { interval } from 'rxjs';
import { catchError, exhaustMap, map, retryWhen, tap } from 'rxjs/operators';
import { createInjector } from './api';
import { LoadGeneratorConfig } from './load-generator-config';
import { Persona, personas } from './personas';
import { genericRetryStrategy, selectPersonaFactory } from './utils';
import { WeightedPersonaFactory } from './weighted-persona-factory';

const operations = ['create', 'read', 'update', 'delete'];

export class LoadGenerator {
  private readonly config: LoadGeneratorConfig;
  private readonly injector: Injector;
  private readonly filteredPersonas: WeightedPersonaFactory[];

  constructor(config: LoadGeneratorConfig) {
    this.config = { ...config };
    this.config.includePersonas = this.config.includePersonas.map(persona => persona.toLowerCase());

    this.injector = createInjector(config.baseUrl, config.accessToken);

    this.filteredPersonas =
      this.config.includePersonas.length > 0
        ? personas.filter(persona =>
            this.config.includePersonas.includes(persona.name.toLowerCase())
          )
        : personas;
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

    cli.table(this.filteredPersonas, {
      name: { header: 'Persona' },
      section: {},
      type: {},
      weight: {}
    });

    const totalWeight = this.filteredPersonas.reduce(
      (agg: number, curr: WeightedPersonaFactory) => agg + curr.weight,
      0
    );

    const errorHandler = this.injector.get(ErrorHandler);

    console.log("\nLet's generate load...\n");

    return interval((60 / this.config.usersPerMinute) * 1000)
      .pipe(
        map(() => {
          const persona = selectPersonaFactory(this.filteredPersonas, totalWeight);
          if (persona === undefined) {
            throw new Error('No personas found!');
          }

          return persona.create(this.config, this.injector);
        }),
        exhaustMap((persona: Persona) => {
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

  async document() {
    console.log("Let's Generate Personas");
    console.log('=======================');
    console.log();
    console.log(`*This document has been generated with \`npm run readme:lets-generate\`*`);
    console.log();

    const personasGroupedBySection = groupBy(personas, 'section');
    const personasGroupedByType = groupBy(personas, 'type');
    const matrix = Object.keys(personasGroupedBySection).map(section =>
      Object.keys(personasGroupedByType).reduce(
        (agg, curr) => ({
          ...agg,
          [curr]: personasGroupedBySection[section].filter(persona => persona.type === curr).length
        }),
        { name: section }
      )
    );

    console.log('## Overview');
    console.log();
    console.log(`| ${['section', ...operations].map(capitalize).join(' | ')} |`);
    console.log(`| ${['section', ...operations].map(op => op.replace(/./g, '-')).join(' | ')} |`);
    matrix.forEach(line => {
      console.log(
        `| ${capitalizeWords(line.name.replace(/-/, ' '))} | ${operations
          .map(op => line[op])
          .join(' | ')} |`
      );
    });

    Object.keys(personasGroupedBySection).forEach(section => {
      console.log();
      console.log(`## ${capitalizeWords(section.replace(/-/, ' '))} Related Personas`);

      const byType = groupBy(personasGroupedBySection[section], 'type');
      operations.forEach(type => {
        const personasOfType = byType[type];

        if (personasOfType !== undefined) {
          personasOfType.forEach(persona => {
            console.log();
            console.log(
              `### [${persona.name}](src/lib/personas/${persona.name.toLowerCase()}.persona.ts) - ${
                persona.type
              }`
            );
            console.log();
            console.log(persona.description);
          });
        }
      });
    });
  }
}
