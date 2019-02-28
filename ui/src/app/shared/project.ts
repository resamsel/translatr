import {Member} from "./member";
import {Locale} from "./locale";
import {Key} from "./key";

export interface Project {
  id: string;
  whenCreated: Date;
  whenUpdated: Date;
  name: string;
  ownerId: string;
  ownerName: string;
  locales: Array<Locale>;
  keys: Array<Key>;
  members: Array<Member>;
}
