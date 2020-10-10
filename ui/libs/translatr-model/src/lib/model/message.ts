import { ProjectEmbedded } from './project-embedded';
import { Temporal } from './temporal';

export interface Message extends ProjectEmbedded, Temporal {
  id?: string;

  localeId: string;
  localeName?: string;
  localeDisplayName?: string;

  keyId: string;
  keyName?: string;

  value: string;
  wordCount?: number;

  dirty?: boolean;
  originalValue?: string;
}
