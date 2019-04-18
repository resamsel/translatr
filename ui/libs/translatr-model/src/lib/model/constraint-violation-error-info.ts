import {ConstraintViolation} from "@dev/translatr-model/src/lib/model/constraint-violation";

export interface ConstraintViolationErrorInfo {
  message: string;
  type: string;
  violations?: Array<ConstraintViolation>;
}
