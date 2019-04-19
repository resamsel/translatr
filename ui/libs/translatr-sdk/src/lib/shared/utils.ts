import * as runes from 'runes';
import {HttpErrorResponse} from "@angular/common/http";
import {ConstraintViolation, ConstraintViolationErrorInfo} from "@dev/translatr-model";

export const firstChar = (name: string): string => {
  if (name === undefined || name.length === 0) {
    return name;
  }
  return runes.substr(name, 0, 1);
};

export const errorMessage = (error: HttpErrorResponse | ConstraintViolationErrorInfo): string => {
  console.log('errorMessage', error);
  if (error instanceof HttpErrorResponse) {
    if (!!error.error && !!error.error.error) {
      return errorMessage(error.error.error);
    }

    return error.message;
  }

  if (!!error.violations) {
    return error.violations.map((v: ConstraintViolation) => `${v.field}: ${v.message}`).join(', ');
  }

  return error.message;
};
