export interface PagedList<T> {
  list: Array<T>;
  hasNext: boolean;
  hasPrev: boolean;
  limit: number;
  offset: number;
  total?: number;
}
