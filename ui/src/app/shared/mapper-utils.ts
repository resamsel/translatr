export interface Temporal {
  whenCreated: Date | string;
  whenUpdated: Date | string
}

export const convertTemporals = <T extends Temporal>(t: T): T => {
  return {
    ...(t as object),
    whenCreated: new Date(t.whenCreated),
    whenUpdated: new Date(t.whenUpdated)
  } as T;
};

export const convertTemporalsList = <T extends Temporal>(list?: Array<T>): Array<T> | undefined => {
  console.log('convertTemporalsList', list);
  if (list === undefined) {
    return undefined;
  }

  return list.map(convertTemporals);
};
