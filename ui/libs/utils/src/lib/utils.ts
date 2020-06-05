import { merge, Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';

export const pickRandomly = <T>(options: T[]): T => {
  return options[Math.ceil(Math.random() * options.length) - 1];
};

/**
 * Calculates "Cartesian Product" sets.
 * @example
 *   cartesianProduct([[1,2], [4,8], [16,32]])
 *   Returns:
 *   [
 *     [1, 4, 16],
 *     [1, 4, 32],
 *     [1, 8, 16],
 *     [1, 8, 32],
 *     [2, 4, 16],
 *     [2, 4, 32],
 *     [2, 8, 16],
 *     [2, 8, 32]
 *   ]
 * @see https://stackoverflow.com/a/36234242/1955709
 * @see https://en.wikipedia.org/wiki/Cartesian_product
 */
export function cartesianProduct<T>(arr: T[][]): T[][] {
  return arr.reduce(
    (a, b) => {
      return a
        .map((x) => {
          return b.map((y) => {
            return x.concat(y);
          });
        })
        .reduce((c, d) => c.concat(d), []);
    },
    [[]] as T[][]
  );
}

export const trackByFn = (index: number, item: { id?: string | number }): string => `${item.id}`;

export const pickKeys = <T>(obj: T, keys: Array<keyof T>): {} => {
  return Object.keys(obj)
    .filter((key) => keys.includes(key as keyof T))
    .reduce((o, key) => {
      return {
        ...o,
        [key]: obj[key]
      };
    }, {});
};

export const slicePagedList = <T, U extends { list: T[] }>(
  pagedList: U | undefined,
  endExclusive: number,
  compareFn: (a: T, b: T) => number = () => 0
): U | undefined => {
  if (!pagedList || !pagedList.list) {
    return undefined;
  }

  return {
    ...pagedList,
    list: pagedList.list
      .slice()
      .sort(compareFn)
      .slice(0, endExclusive)
  };
};

export const mergeWithError = <T, E>(
  entity$: Observable<T>,
  error$: Observable<E>
): Observable<[T, undefined] | [undefined, E]> => {
  return merge(
    entity$.pipe(
      filter((x) => !!x),
      map<T, [T, undefined]>((entity: T) => [entity, undefined])
    ),
    error$.pipe(
      filter((x) => !!x),
      map<E, [undefined, E]>((error: E) => [undefined, error])
    )
  );
};
