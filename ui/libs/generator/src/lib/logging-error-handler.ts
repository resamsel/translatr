import { HttpErrorResponse } from '@angular/common/http';
import { BUILD_INFO } from '@dev/translatr-model';
import { ErrorHandler, RestRequest } from '@dev/translatr-sdk';
import { Observable, throwError } from 'rxjs';

const errorTemplate = (error: HttpErrorResponse, request?: RestRequest): string => `
Error Report
============

Source: Translatr SDK
Date: ${new Date()}
Build: ${BUILD_INFO.buildVersion}
Request: ${JSON.stringify(request, null, 2)}
Error: ${JSON.stringify(error, null, 2)}
------------
`;

export class LoggingErrorHandler extends ErrorHandler {
  handleError(error: HttpErrorResponse, request?: RestRequest): Observable<any> {
    if (error.status === 0) {
      // Connection issue
      console.error(error.statusText);
      return throwError(error.statusText);
    }

    if (
      error.error?.error?.violations !== undefined &&
      error.error?.error?.violations[0]?.message !== 'error.nameunique' &&
      error.error?.error?.violations[0]?.message !== 'error.usernameunique' &&
      error.error?.error?.violations[0]?.message !== 'error.projectnameunique' &&
      error.error?.error?.violations[0]?.message !== 'Entry already exists (duplicate key)'
    ) {
      console.error(errorTemplate(error, request));
    }

    return super.handleError(error, request);
  }
}
