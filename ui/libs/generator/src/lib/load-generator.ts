import { Injector } from '@angular/core';
import { selectPersonaFactory } from './utils';
import * as dateformat from 'dateformat';
import { interval } from 'rxjs';
import { concatMap, map } from 'rxjs/operators';
import { createInjector } from './api';
import { LoadGeneratorConfig } from './load-generator-config';
import { personas } from './personas';
import { WeightedPersonaFactory } from './weighted-persona-factory';

const totalWeight = personas.reduce(
  (agg: number, curr: WeightedPersonaFactory) => agg + curr.weight,
  0
);

export class LoadGenerator {
  private readonly config: LoadGeneratorConfig;
  private readonly injector: Injector;

  constructor(config: LoadGeneratorConfig) {
    this.config = { ...config };

    this.injector = createInjector(config.baseUrl, config.accessToken);
  }

  async execute() {
    personas.forEach(personaFactory =>
      console.log(
        `${personaFactory.create(this.config, this.injector).name}: ${personaFactory.weight}`
      )
    );
    interval((60 / this.config.requestsPerMinute) * 1000)
      .pipe(
        map(() => selectPersonaFactory(personas, totalWeight).create(this.config, this.injector)),
        concatMap(persona => persona.execute().pipe(map(message => `[${persona.name}] ${message}`)))
      )
      .subscribe(message => console.log(`${dateformat('yyyy-mm-dd hh:MM:ss')} - ${message}`));
  }
}
