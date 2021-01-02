import { merge, Observable } from 'rxjs';
import { map, skip } from 'rxjs/operators';

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
        .map(x => {
          return b.map(y => {
            return x.concat(y);
          });
        })
        .reduce((c, d) => c.concat(d), []);
    },
    [[]] as T[][]
  );
}

export const trackByFn = (index: number, item: { id?: string | number }): string => `${item.id}`;

export const pickKeys = <T>(obj: T, keys: (keyof T)[]): {} => {
  if (obj === undefined || obj === null) {
    return {};
  }

  return Object.keys(obj)
    .filter(key => keys.includes(key as keyof T))
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
      skip(1),
      map<T, [T, undefined]>((entity: T) => [entity, undefined])
    ),
    error$.pipe(
      skip(1),
      map<E, [undefined, E]>((error: E) => [undefined, error])
    )
  );
};

export const capitalize = (s: string): string => {
  if (s === undefined || s.length === 0) {
    return s;
  }

  return s[0].toUpperCase() + s.substring(1);
};

export const capitalizeWords = (s: string): string => {
  return s
    .split(' ')
    .map(capitalize)
    .join(' ');
};

export const cutOffAfter = (secret: string, length: number): string => {
  return secret.substr(0, length) + (secret.length > length ? '...' : '');
};

export const groupBy = <T>(xs: T[], key): Record<string, T[]> => {
  return xs.reduce((rv, x) => {
    (rv[x[key]] = rv[x[key]] || []).push(x);
    return rv;
  }, {});
};
