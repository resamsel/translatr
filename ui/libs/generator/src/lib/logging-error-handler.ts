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

const excludedViolations = [
  'error.nameunique',
  'error.usernameunique',
  'error.projectnameunique',
  'error.accesstokennameunique',
  'Entry already exists (duplicate key)'
];

export class LoggingErrorHandler extends ErrorHandler {
  handleError(error: HttpErrorResponse, request?: RestRequest): Observable<any> {
    if (error.status === 0) {
      // Connection issue
      console.error(error.statusText);
      return throwError(error.statusText);
    }

    if (
      error.error?.error?.violations !== undefined &&
      !excludedViolations.includes(error.error?.error?.violations[0]?.message)
    ) {
      console.error(errorTemplate(error, request));
    }

    return super.handleError(error, request);
  }
}
