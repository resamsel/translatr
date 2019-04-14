import { ConstraintViolationErrorInfo } from "@dev/translatr-sdk/src/lib/shared/constraint-violation-error-info";
import { ConstraintViolation } from "@dev/translatr-sdk/src/lib/shared/constraint-violation";

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

export const pickRandomly = <T>(options: Array<T>): T => {
  return options[Math.ceil(Math.random() * options.length) - 1];
};

export const errorMessage = (error: ConstraintViolationErrorInfo): string => {
  if (!!error.violations) {
    return error.violations.map((v: ConstraintViolation) => `${v.field}: ${v.message}`).join(', ');
  }

  return error.message;
};
