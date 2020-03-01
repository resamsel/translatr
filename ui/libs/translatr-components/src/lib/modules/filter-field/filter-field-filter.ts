export interface FilterFieldFilter {
  key: string;
  type: 'string' | 'number' | 'boolean' | 'option';
  value?: string | number | boolean;
  title?: string;
  group?: string;
  allowEmpty?: boolean;
}
