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

  locales?: Array<Locale>;
  keys?: Array<Key>;
  members?: Array<Member>;
  messages?: Array<Message>;

  wordCount?: number;
  myRole?: MemberRole;
}
