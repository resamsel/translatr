import { Temporal } from '@translatr/translatr-model/src';
import { Message } from './message';
import { ProjectEmbedded } from './project-embedded';

export interface Locale extends ProjectEmbedded, Temporal {
  id?: string;

  name: string;
  displayName?: string;
  progress?: number;

  messages?: { [key: string]: Message };
}
