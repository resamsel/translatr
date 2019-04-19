import {ConstraintViolation, ConstraintViolationErrorInfo} from "@dev/translatr-model";
import {HttpErrorResponse} from "@angular/common/http";

export const envAsString = (key: string, defaultValue: string): string => {
  if (process.env[key]) {
    return process.env[key];
  }

  return defaultValue;
};

export const envAsNumber = (key: string, defaultValue: number): number => {
  if (process.env[key]) {
    return parseInt(process.env[key], 10);
  }

  return defaultValue;
};

export const pickRandomly = <T>(options: Array<T>): T => {
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
 * @param arr {T[][]}
 * @returns {T[][]}
 */
export function cartesianProduct<T> (arr: T[][]): T[][] {
  return arr.reduce((a, b) => {
    return a.map(x => {
      return b.map(y => {
        return x.concat(y)
      })
    }).reduce((c, d) => c.concat(d), [])
  }, [[]] as T[][])
}
