import { ProjectEmbedded, Temporal, UserEmbedded } from '@dev/translatr-model';
import { MemberRole } from './member-role';

export interface Member extends ProjectEmbedded, UserEmbedded, Temporal {
  id?: number;

  role: MemberRole;
}
