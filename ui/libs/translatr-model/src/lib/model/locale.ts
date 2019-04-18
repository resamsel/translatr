import { Message } from "./message";

export interface Locale {
  id?: string;
  whenCreated?: Date;
  whenUpdated?: Date;

  name: string;
  displayName?: string;

  projectId: string;
  projectName?: string;
  projectOwnerUsername?: string;

  messages?: { [key: string]: Message };
}
