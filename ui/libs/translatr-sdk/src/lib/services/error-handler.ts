import { HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';

export interface RestRequest {
  name: string;
  params: any[];
  method: 'get' | 'post' | 'put' | 'delete';
  path: string;
}

export class ErrorHandler {
  handleError(error: HttpErrorResponse, request?: RestRequest): Observable<never> {
    console.log(
      'Handling error for %s:\n%s',
      JSON.stringify(request),
      JSON.stringify(error)
    );
    return throwError(error);
  }
}
