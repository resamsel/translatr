import { Message } from "./message";
import { ProjectEmbedded } from "./project-embedded";

export interface Locale extends ProjectEmbedded {
  id?: string;
  whenCreated?: Date;
  whenUpdated?: Date;

  name: string;
  displayName?: string;

  messages?: { [key: string]: Message };
}
