import { Injector } from '@angular/core';
import { LoadGeneratorConfig } from './load-generator-config';
import { Persona } from './personas';

export interface WeightedPersonaFactory {
  create: (config: LoadGeneratorConfig, injector: Injector) => Persona;
  weight: number;
}
