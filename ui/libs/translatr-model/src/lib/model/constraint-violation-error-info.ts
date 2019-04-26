import { ConstraintViolation } from './constraint-violation';
import { ErrorInfo } from './error-info';

export interface ConstraintViolationErrorInfo extends ErrorInfo {
  violations?: Array<ConstraintViolation>;
}
