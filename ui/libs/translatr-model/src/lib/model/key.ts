import { KeyDto } from '../generated/model/keyDto';
import { Message } from './message';

export interface Key extends Omit<KeyDto, 'whenCreated' | 'whenUpdated'> {
  whenCreated?: Date;
  whenUpdated?: Date;

  messages?: { [key: string]: Message };
}
