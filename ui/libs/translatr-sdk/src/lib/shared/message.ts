export interface Message {
  id?: string;
  whenCreated?: Date;
  whenUpdated?: Date;

  localeId: string;
  localeName: string;

  keyId: string;
  keyName: string;

  value: string;
}
