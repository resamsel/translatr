import { Temporal } from '.';

export interface AccessToken extends Temporal {
  id?: number;

  userId: string;
  userName?: string;
  userUsername?: string;

  name: string;
  key?: string;
  scope: string;
}
