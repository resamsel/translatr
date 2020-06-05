import { Temporal } from '@translatr/translatr-model/src';
import { Message } from './message';
import { ProjectEmbedded } from './project-embedded';

export interface Key extends ProjectEmbedded, Temporal {
  id?: string;

  name: string;
  progress?: number;

  messages?: { [key: string]: Message };
}
