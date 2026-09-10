import { toggleSuffix } from './toggle-suffix';

/**
 * Spec source of truth:
 *   openspec/changes/fix-generator-nullable-suffix-toggle/specs/load-generator-personas/spec.md
 *   Requirement: "Suffix-toggle mutations tolerate absent source fields"
 */
describe('toggleSuffix', () => {
  it('treats undefined as the empty string and appends the marker', () => {
    expect(toggleSuffix(undefined, '!')).toBe('!');
  });

  it('treats null as the empty string and appends the marker', () => {
    expect(toggleSuffix(null, '!')).toBe('!');
  });

  it('treats an empty string as absent and appends the marker', () => {
    expect(toggleSuffix('', '!')).toBe('!');
  });

  it('appends the marker when the value does not end with it', () => {
    expect(toggleSuffix('Generated', '!')).toBe('Generated!');
  });

  it('strips the marker when the value ends with it (round-trip)', () => {
    expect(toggleSuffix('Generated!', '!')).toBe('Generated');
  });

  it('only strips a trailing marker, not one that appears internally', () => {
    expect(toggleSuffix('Hello! world', '!')).toBe('Hello! world!');
  });

  it('supports multi-character suffixes such as _formal', () => {
    expect(toggleSuffix('de', '_formal')).toBe('de_formal');
    expect(toggleSuffix('de_formal', '_formal')).toBe('de');
  });
});
