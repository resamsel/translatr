import {ConstraintViolation} from "@dev/translatr-sdk/src/lib/shared/constraint-violation";

export interface ConstraintViolationErrorInfo {
  message: string;
  type: string;
  violations?: Array<ConstraintViolation>;
}
