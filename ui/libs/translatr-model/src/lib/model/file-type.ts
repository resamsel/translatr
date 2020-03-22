export enum FileType {
  JavaProperties = 'java_properties',
  PlayMessages = 'play_messages',
  Gettext = 'gettext',
  Json = 'json'
}

export const fileTypes = [
  FileType.JavaProperties,
  FileType.PlayMessages,
  FileType.Gettext,
  FileType.Json
];

export const fileTypeNames = {
  [FileType.JavaProperties]: 'Java Properties',
  [FileType.PlayMessages]: 'Play Messages',
  [FileType.Gettext]: 'Gettext',
  [FileType.Json]: 'JSON'
};
