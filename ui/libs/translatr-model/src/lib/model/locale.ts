import { LocaleDto } from '../generated/model/localeDto';
import { Message } from './message';

export interface Locale extends Omit<LocaleDto, 'whenCreated' | 'whenUpdated'> {
  whenCreated?: Date;
  whenUpdated?: Date;

  messages?: { [key: string]: Message };
}
