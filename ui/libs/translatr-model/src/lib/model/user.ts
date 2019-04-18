import {Member} from "./member";
import {UserRole} from "@dev/translatr-model/src/lib/model/user-role";

export interface User {
  id?: string;
  whenCreated?: Date;
  whenUpdated?: Date;
  name: string;
  username: string;
  email: string;
  role: UserRole;
  memberships?: Member[];
}
