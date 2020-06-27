import { Temporal } from '.';

export interface AccessToken extends Temporal {
  id?: number;

  userId: string;
  userUsername?: string;
  userName?: string;

  name: string;
  key?: string;
  scope: string;
}
