export enum VersionUpdateType {
  JSON = 'json',
  FILE = 'file'
}

export interface BaseVersionUpdate {
  type: string;
}

export class JsonUpdate implements BaseVersionUpdate {
  readonly type = VersionUpdateType.JSON;

  readonly file: string;
}

export class FileUpdate implements BaseVersionUpdate {
  readonly type = VersionUpdateType.FILE;

  readonly file: string;

  readonly search: string;

  readonly replace: string;
}

export type VersionUpdate = JsonUpdate | FileUpdate;
