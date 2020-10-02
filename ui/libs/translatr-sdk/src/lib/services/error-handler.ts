import { HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';

export interface RestRequest {
  name: string;
  params: any[];
  method: 'get' | 'post' | 'put' | 'patch' | 'delete';
  path: string;
}

export class ErrorHandler {
  handleError(error: HttpErrorResponse, request?: RestRequest): Observable<never> {
    return throwError(error);
  }
}
