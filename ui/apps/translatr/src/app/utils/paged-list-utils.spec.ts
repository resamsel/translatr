import { pagedListDelete } from './paged-list-utils';

describe('paged-list-utils', () => {
  describe('pagedListDelete', () => {
    it('should decrease the total when removing from the list', () => {
      // given
      const pagedList = {
        list: [{ id: 1 }],
        hasNext: false,
        hasPrev: false,
        total: 1,
        limit: 20,
        offset: 0
      };
      const payload = { id: 1 };

      // when
      const actual = pagedListDelete(pagedList, payload);

      // then
      expect(actual.list).toEqual([]);
      expect(actual.total).toBe(0);
    });

    it('should keep the total when not removing from the list', () => {
      // given
      const pagedList = {
        list: [{ id: 1 }],
        hasNext: false,
        hasPrev: false,
        total: 1,
        limit: 20,
        offset: 0
      };
      const payload = { id: 2 };

      // when
      const actual = pagedListDelete(pagedList, payload);

      // then
      expect(actual.list).toEqual([{ id: 1 }]);
      expect(actual.total).toBe(1);
    });
  });
});
