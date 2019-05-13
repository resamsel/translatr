import {Message} from './message';
import {ProjectEmbedded} from './project-embedded';

export interface Key extends ProjectEmbedded {
  id?: string;
  whenCreated?: Date;
  whenUpdated?: Date;

  name: string;

  messages?: { [key: string]: Message };
}
