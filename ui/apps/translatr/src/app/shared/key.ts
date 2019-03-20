import { Message } from "./message";

export interface Key {
  id: string;
  whenCreated: Date;
  whenUpdated: Date;
  name: string;

  messages: { [key: string]: Message };
}
