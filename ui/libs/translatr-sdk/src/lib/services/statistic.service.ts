import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Statistic } from '@dev/translatr-model';
import { Observable } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ErrorHandler } from './error-handler';

@Injectable({
  providedIn: 'root'
})
export class StatisticService {
  constructor(private readonly http: HttpClient, private readonly errorHandler: ErrorHandler) {}

  find(): Observable<Statistic> {
    const path = '/api/statistics';
    return this.http.get<Statistic>(path).pipe(
      catchError((err: HttpErrorResponse) =>
        this.errorHandler.handleError(err, {
          name: 'find',
          params: [],
          method: 'get',
          path
        })
      )
    );
  }
}
