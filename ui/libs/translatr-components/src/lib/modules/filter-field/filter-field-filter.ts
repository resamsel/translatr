import { FilterFieldSelection } from './filter-field-selection';

export interface FilterFieldFilter extends FilterFieldSelection {
  title?: string;
  group?: string;
  allowEmpty?: boolean;
  type: 'string' | 'number' | 'boolean';
}
