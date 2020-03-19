import { envAsString } from '@translatr/node-utils';

describe('utils', () => {
  describe('envAsString', () => {
    it('should return default value', () => {
      // given
      const defaultValue = 'default';

      // when
      const actual = envAsString('__A', defaultValue);

      // then
      expect(actual).toBe(defaultValue);
    });
  });
});
