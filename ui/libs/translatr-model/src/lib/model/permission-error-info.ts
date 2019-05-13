import { ErrorInfo } from './error-info';

export interface PermissionErrorInfo extends ErrorInfo {
  scopes: string[];
}
