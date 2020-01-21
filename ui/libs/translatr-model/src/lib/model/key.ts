import { Message } from './message';
import { ProjectEmbedded } from './project-embedded';
import { Temporal } from '@translatr/translatr-model/src';

export interface Key extends ProjectEmbedded, Temporal {
  id?: string;

  name: string;
  progress?: number;

  messages?: { [key: string]: Message };
}
