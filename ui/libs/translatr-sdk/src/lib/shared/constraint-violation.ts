export interface ConstraintViolation {
  message: string;
  field: string;
  invalidValue?: any;
}
