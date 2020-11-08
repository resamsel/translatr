import { cartesianProduct } from '@translatr/utils';

export const localeNames = [
  'en',
  'de',
  'it',
  'fr',
  'hu',
  'sl',
  'cs',
  'es',
  'pl',
  'el',
  'el_GR',
  'en-US',
  'en-UK',
  'de-AT',
  'de-DE',
  'ar',
  'ro',
  'es',
  'sw',
  'th',
  'ja',
  'zh',
  'ca',
  'ko'
];
export const featureNames = [
  'user',
  'users',
  'project',
  'projects',
  'locale',
  'locales',
  'key',
  'keys',
  'message',
  'messages',
  'accessToken',
  'accessTokens',
  'member',
  'members',
  'activity',
  'activities',
  'dashboard',
  'admin'
];
export const parts = ['list', 'detail', 'find', 'search', 'main', 'header', 'footer'];
export const keySuffixes = [
  'title',
  'description',
  'text',
  'comment',
  'get',
  'create',
  'update',
  'delete',
  'permission',
  'sell',
  'confirm',
  'allow',
  'restricted',
  'filter',
  'clear'
];
export const keyNames = cartesianProduct([
  featureNames,
  parts,
  keySuffixes
]).map((values: string[]) => values.join('.'));

export const messageSuffix = '!';
