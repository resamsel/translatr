import { Injector } from '@angular/core';
import * as dateformat from 'dateformat';
import { interval } from 'rxjs';
import { concatMap, map } from 'rxjs/operators';
import { createInjector } from './api';
import { LoadGeneratorConfig } from './load-generator-config';
import { personas } from './personas';
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

    filteredPersonas.forEach(personaFactory =>
      console.log(`${personaFactory.name}: ${personaFactory.weight}`)
    );

    const totalWeight = filteredPersonas.reduce(
      (agg: number, curr: WeightedPersonaFactory) => agg + curr.weight,
      0
    );

    interval((60 / this.config.requestsPerMinute) * 1000)
      .pipe(
        map(() =>
          selectPersonaFactory(filteredPersonas, totalWeight).create(this.config, this.injector)
        ),
        concatMap(persona => persona.execute().pipe(map(message => `[${persona.name}] ${message}`)))
      )
      .subscribe(message => console.log(`${dateformat('yyyy-mm-dd hh:MM:ss')} - ${message}`));
  }
}
