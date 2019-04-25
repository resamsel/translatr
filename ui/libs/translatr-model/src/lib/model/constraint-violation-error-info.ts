import {ConstraintViolation} from './constraint-violation';

export interface ConstraintViolationErrorInfo {
  message: string;
  type: string;
  violations?: Array<ConstraintViolation>;
}
