import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { convertTemporals } from '../shared/mapper-utils';
import { Aggregate, PagedList, RequestCriteria, User } from '@dev/translatr-model';
import { AbstractService, encodePathParam } from './abstract.service';
import { ErrorHandler } from './error-handler';

@Injectable({
  providedIn: 'root'
})
export class UserService extends AbstractService<User, RequestCriteria> {
  constructor(
    http: HttpClient,
    errorHandler: ErrorHandler
  ) {
    super(
      http,
      errorHandler,
      () => '/api/users',
      '/api/user'
    );
  }

  byUsername(
    username: string,
    options?: {
      params?:
        | HttpParams
        | {
        [param: string]: string | string[];
      };
    }
  ): Observable<User | undefined> {
    return this.http
      .get<User>(`/api/${encodePathParam(username)}`, options)
      .pipe(
        map(convertTemporals),
        catchError((err: HttpErrorResponse) =>
          this.errorHandler.handleError(err))
      );
  }

  me(params: Record<string, string> = {}): Observable<User | undefined> {
    return this.http.get<User>('/api/me', {params})
      .pipe(
        map(convertTemporals),
        catchError((err: HttpErrorResponse) =>
          this.errorHandler.handleError(err))
      );
  }

  activity(userId: string): Observable<PagedList<Aggregate> | undefined> {
    return this.http.get<PagedList<Aggregate>>(`/api/user/${userId}/activity`)
      .pipe(
        catchError((err: HttpErrorResponse) =>
          this.errorHandler.handleError(err))
      );
  }
}
