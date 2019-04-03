import * as runes from 'runes';

export const firstChar = (name: string): string => {
  if(name === undefined || name.length === 0) {
    return name;
  }
  return runes.substr(name, 0, 1);
}
