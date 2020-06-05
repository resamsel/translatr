export interface PagedList<T> {
  list: T[];
  hasNext: boolean;
  hasPrev: boolean;
  limit: number;
  offset: number;
  total?: number;
}
