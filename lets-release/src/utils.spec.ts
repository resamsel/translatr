import {expect} from '@oclif/test';
import {run} from './utils';

describe('utils', () => {
  describe('run', () => {
    it('should run it', () => {
      run('blubb', () => Promise.resolve(true)).then(actual =>
        expect(actual).eq(true)
      );
    });
  });
});
