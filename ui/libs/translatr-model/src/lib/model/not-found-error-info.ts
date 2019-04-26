import { ErrorInfo } from './error-info';

export interface NotFoundErrorInfo extends ErrorInfo {
  entity: string;
  id: string;
}
