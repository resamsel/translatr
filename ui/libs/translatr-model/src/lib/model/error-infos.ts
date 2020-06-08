import {
  ConstraintViolationErrorInfo,
  NotFoundErrorInfo,
  PermissionErrorInfo
} from '@dev/translatr-model';

export type ErrorInfos = ConstraintViolationErrorInfo | NotFoundErrorInfo | PermissionErrorInfo;
