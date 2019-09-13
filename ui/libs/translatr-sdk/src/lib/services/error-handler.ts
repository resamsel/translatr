import { HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';

export class ErrorHandler {
  handleError(error: HttpErrorResponse): Observable<never> {
    console.log('Handle error', error);
    return throwError(error);
  }
}
