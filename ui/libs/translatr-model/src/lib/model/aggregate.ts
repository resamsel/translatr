import { AggregateDto } from '../generated/model/aggregateDto';

export interface Aggregate extends Omit<AggregateDto, 'date'> {
  date: Date;
}
