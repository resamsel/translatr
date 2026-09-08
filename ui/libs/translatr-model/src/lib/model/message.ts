import { MessageDto } from '../generated/model/messageDto';

export interface Message extends MessageDto {
  projectOwnerUsername?: string;
  dirty?: boolean;
  originalValue?: string;
}
