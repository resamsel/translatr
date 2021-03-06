import { mergeWithError } from '@translatr/utils';
import { ReplaySubject } from 'rxjs';

describe('utils', () => {
  describe('mergeWithError', () => {
    it('should emit entity with single item input', done => {
      // given
      const entity$ = new ReplaySubject<string>();
      const error$ = new ReplaySubject<number>();
      const merged$ = mergeWithError(entity$, error$);

      // when
      entity$.next(undefined);
      error$.next(undefined);
      entity$.next('first');

      // then
      merged$.subscribe(([entity, error]) => {
        expect(entity).toEqual('first');
        expect(error).toBeUndefined();
        done();
      });
    });

    it('should throw error with error input', done => {
      // given
      const entity$ = new ReplaySubject<string>();
      const error$ = new ReplaySubject<number>();
      const merged$ = mergeWithError(entity$, error$);

      // when
      entity$.next(undefined);
      error$.next(undefined);
      error$.next(1);

      // then
      merged$.subscribe(([entity, error]) => {
        expect(entity).toBeUndefined();
        expect(error).toEqual(1);
        done();
      });
    });

    it('should throw two errors with two error inputs', done => {
      // given
      const entity$ = new ReplaySubject<string>();
      const error$ = new ReplaySubject<number>();
      const merged$ = mergeWithError(entity$, error$);
      let count = 0;

      // when
      entity$.next(undefined);
      error$.next(undefined);
      error$.next(1);
      error$.next(2);

      // then
      merged$.subscribe(([entity, error]) => {
        count++;
        expect(entity).toBeUndefined();
        expect(error).toEqual(count);
        if (count === 2) {
          done();
        }
      });
    });
  });
});
