import { HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';

export interface RestRequest {
  name: string;
  params: any[];
  method: 'get' | 'post' | 'put' | 'patch' | 'delete';
  path: string;
}

const errorTemplate = (error: HttpErrorResponse, request?: RestRequest): string => `
Error Report
============

Source: Translatr SDK
Date: ${new Date()}
Request: ${JSON.stringify(request, null, 2)}
Error: ${JSON.stringify(error, null, 2)}
------------
`;

export class ErrorHandler {
  handleError(error: HttpErrorResponse, request?: RestRequest): Observable<never> {
    if (
      error.error?.error?.violations[0]?.message !== 'error.nameunique' &&
      error.error?.error?.violations[0]?.message !== 'Entry already exists (duplicate key)'
    ) {
      console.warn(errorTemplate(error, request));
    }
    return throwError(error);
  }
}
