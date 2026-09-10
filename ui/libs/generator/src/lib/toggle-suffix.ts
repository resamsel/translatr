/**
 * Reversibly toggle a trailing marker on a string field.
 *
 * Personas use this for their "update" mutations: append `suffix` when it is not
 * already there, strip it when it is. A `null` / `undefined` / empty source value
 * is treated as the empty string, so the first toggle yields just the marker
 * instead of throwing (see issue #297 — real projects often have no description).
 *
 * `suffix` is expected to be a non-empty constant; an empty suffix would make the
 * toggle a no-op.
 */
export const toggleSuffix = (value: string | null | undefined, suffix: string): string => {
  const base = value ?? '';
  return base.endsWith(suffix) ? base.slice(0, -suffix.length) : base + suffix;
};
