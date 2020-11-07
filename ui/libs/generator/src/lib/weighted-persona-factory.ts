import { Injector } from '@angular/core';
import { LoadGeneratorConfig } from './load-generator-config';
import { Persona } from './personas';
import { WeightedPersona } from './weighted-persona';

export interface WeightedPersonaFactory extends WeightedPersona {
  create: (config: LoadGeneratorConfig, injector: Injector) => Persona;
}
