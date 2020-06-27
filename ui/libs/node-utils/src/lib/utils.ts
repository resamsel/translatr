export const envAsString = (key: string, defaultValue: string): string => {
  if (process.env[key]) {
    return process.env[key];
  }

  return defaultValue;
};

export const envAsNumber = (key: string, defaultValue: number): number => {
  if (process.env[key]) {
    return parseInt(process.env[key], 10);
  }

  return defaultValue;
};
