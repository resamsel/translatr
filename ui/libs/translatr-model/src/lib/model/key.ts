import { Message } from './message';
import { ProjectEmbedded } from './project-embedded';
import { Temporal } from './temporal';

export interface Key extends ProjectEmbedded, Temporal {
  id?: string;

  name: string;
  progress?: number;

  messages?: { [key: string]: Message };
}
