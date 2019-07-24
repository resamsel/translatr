export enum Scope {
  UserRead = 'read:user',
  UserWrite = 'write:user',
  AccessTokenRead = 'read:accesstoken',
  AccessTokenWrite = 'write:accesstoken',
  ProjectRead = 'read:project',
  ProjectWrite = 'write:project',
  LocaleRead = 'read:locale',
  LocaleWrite = 'write:locale',
  KeyRead = 'read:key',
  KeyWrite = 'write:key',
  MessageRead = 'read:message',
  MessageWrite = 'write:message',
  NotificationRead = 'read:notification',
  NotificationWrite = 'write:notification'
}

export const scopes = [
  Scope.UserRead,
  Scope.UserWrite,
  Scope.AccessTokenRead,
  Scope.AccessTokenWrite,
  Scope.ProjectRead,
  Scope.ProjectWrite,
  Scope.LocaleRead,
  Scope.LocaleWrite,
  Scope.KeyRead,
  Scope.KeyWrite,
  Scope.MessageRead,
  Scope.MessageWrite,
  Scope.NotificationRead,
  Scope.NotificationWrite
];
