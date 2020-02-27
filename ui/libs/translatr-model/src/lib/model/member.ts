import { MemberRole } from './member-role';
import { ProjectEmbedded, Temporal, UserEmbedded } from '@dev/translatr-model';

export interface Member extends ProjectEmbedded, UserEmbedded, Temporal {
  id: number;

  role: MemberRole;
}
