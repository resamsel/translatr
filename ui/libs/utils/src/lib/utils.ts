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
 */
export function cartesianProduct<T> (arr: T[][]): T[][] {
  return arr.reduce((a, b) => {
    return a.map(x => {
      return b.map(y => {
        return x.concat(y);
      });
    }).reduce((c, d) => c.concat(d), []);
  }, [[]] as T[][]);
}
