import { Key, Locale, Member, MemberRole, Message, Temporal } from '.';

export interface Project extends Temporal {
  id?: string;

  name: string;
  description?: string;
  progress?: number;

  ownerId?: string;
  ownerName?: string;
  ownerUsername?: string;
  ownerEmailHash?: string;

  locales?: Locale[];
  keys?: Key[];
  members?: Member[];
  messages?: Message[];

  wordCount?: number;
  myRole?: MemberRole;
}
