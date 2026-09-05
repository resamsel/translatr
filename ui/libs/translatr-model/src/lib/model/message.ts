import { MessagePayload } from '../generated/model/messagePayload';

export interface Message extends MessagePayload {
  projectOwnerUsername?: string;
  dirty?: boolean;
  originalValue?: string;
}
