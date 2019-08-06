import { MemberRole } from './member-role';
import { ProjectEmbedded } from '@dev/translatr-model';

export interface Member extends ProjectEmbedded {
  id: number;
  whenCreated?: Date;
  whenUpdated?: Date;

  role: MemberRole;

  userName?: string;
  userId: string;
  userUsername?: string;
  userEmail?: string;
}
