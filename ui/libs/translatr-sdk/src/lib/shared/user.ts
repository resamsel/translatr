import {Member} from "./member";

export interface User {
  id: string;
  whenCreated: Date;
  whenUpdated: Date;
  name: string;
  username: string;
  email: string;
  memberships?: Member[];
}
