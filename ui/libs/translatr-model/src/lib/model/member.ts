import { MemberRole } from './member-role';
import { ProjectEmbedded, Temporal } from '@dev/translatr-model';

export interface Member extends ProjectEmbedded, Temporal {
  id: number;

  role: MemberRole;

  userName?: string;
  userId: string;
  userUsername?: string;
  userEmail?: string;
}
