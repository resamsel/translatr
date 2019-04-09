import {Member} from "./member";
import {Locale} from "./locale";
import {Key} from "./key";
import {Message} from "./message";

export interface Project {
  id: string;
  whenCreated?: Date;
  whenUpdated?: Date;
  name: string;
  ownerId?: string;
  ownerName?: string;
  ownerUsername?: string;
  locales?: Array<Locale>;
  keys?: Array<Key>;
  members?: Array<Member>;
  messages?: Array<Message>;
}
