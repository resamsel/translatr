import {ConstraintViolationErrorInfo} from './constraint-violation-error-info';

export interface ErrorAction {
  payload: {
    error: {
      error: ConstraintViolationErrorInfo
    }
  };
}
