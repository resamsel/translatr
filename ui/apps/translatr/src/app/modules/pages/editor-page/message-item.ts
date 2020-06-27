import { Key, Locale, Message } from '@dev/translatr-model';

export class MessageItem {
  locale: Locale;
  key: Key;
  message?: Message;
  selected: boolean;
}
