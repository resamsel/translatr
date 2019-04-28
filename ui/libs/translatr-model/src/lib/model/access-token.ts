export interface AccessToken {
  id?: number;
  whenCreated?: Date;
  whenUpdated?: Date;

  userId: string;
  userName?: string;
  userUsername?: string;

  name: string;
  key?: string;
  scope: string;
}
