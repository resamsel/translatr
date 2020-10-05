import { Injector } from '@angular/core';
import { LoadGeneratorConfig } from './load-generator-config';
import { Persona } from './personas';

export interface WeightedPersonaFactory {
  name: string;
  weight: number;
  create: (config: LoadGeneratorConfig, injector: Injector) => Persona;
}
