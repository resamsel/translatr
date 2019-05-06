import { ProjectEmbedded } from "./project-embedded";

export interface Message extends ProjectEmbedded {
  id?: string;
  whenCreated?: Date;
  whenUpdated?: Date;

  localeId: string;
  localeName?: string;
  localeDisplayName?: string;

  keyId: string;
  keyName?: string;

  value: string;
}
