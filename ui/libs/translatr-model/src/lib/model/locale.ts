import { Message } from './message';
import { ProjectEmbedded } from './project-embedded';
import { Temporal } from '@translatr/translatr-model/src';

export interface Locale extends ProjectEmbedded, Temporal {
  id?: string;

  name: string;
  displayName?: string;

  messages?: { [key: string]: Message };
}
