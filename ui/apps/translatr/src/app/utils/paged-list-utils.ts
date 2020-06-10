import { Identifiable, PagedList } from '@dev/translatr-model';

export const pagedListInsert = <T extends Identifiable>(
  pagedList: PagedList<T>,
  payload: T
): PagedList<T> => ({
  ...pagedList,
  list: [...pagedList.list, payload],
  total: pagedList.total + 1
});

export const pagedListUpdate = <T extends Identifiable>(
  pagedList: PagedList<T>,
  payload: T
): PagedList<T> => ({
  ...pagedList,
  list: pagedList.list.map(i => (i.id === payload.id ? payload : i))
});

export const pagedListDelete = <T extends Identifiable>(
  pagedList: PagedList<T>,
  payload: T
): PagedList<T> => ({
  ...pagedList,
  list: pagedList.list.filter(i => i.id !== payload.id),
  total: pagedList.total - pagedList.list.filter(i => i.id === payload.id).length
});
