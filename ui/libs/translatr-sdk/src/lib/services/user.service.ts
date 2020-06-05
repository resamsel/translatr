import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Aggregate, PagedList, RequestCriteria, Setting, User } from '@dev/translatr-model';
import { Observable } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { convertTemporals } from '../shared/mapper-utils';
import { AbstractService, encodePathParam, RequestOptions } from './abstract.service';
import { ErrorHandler } from './error-handler';

@Injectable({
  providedIn: 'root'
})
export class UserService extends AbstractService<User, RequestCriteria> {
  constructor(http: HttpClient, errorHandler: ErrorHandler) {
    super(http, errorHandler, () => '/api/users', '/api/user');
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
    const path = `/api/${encodePathParam(username)}`;
    return this.http.get<User>(path, options).pipe(
      map(convertTemporals),
      catchError((err: HttpErrorResponse) =>
        this.errorHandler.handleError(err, {
          name: 'byUsername',
          params: [username, options],
          method: 'get',
          path
        })
      )
    );
  }

  me(params: Record<string, string> = {}): Observable<User | undefined> {
    const path = '/api/me';
    return this.http
      .get<User>(path, { params })
      .pipe(
        map(convertTemporals),
        catchError((err: HttpErrorResponse) =>
          this.errorHandler.handleError(err, {
            name: 'me',
            params: [params],
            method: 'get',
            path
          })
        )
      );
  }

  activity(userId: string): Observable<PagedList<Aggregate> | undefined> {
    const path = `/api/user/${userId}/activity`;
    return this.http.get<PagedList<Aggregate>>(path).pipe(
      catchError((err: HttpErrorResponse) =>
        this.errorHandler.handleError(err, {
          name: 'activity',
          params: [userId],
          method: 'get',
          path
        })
      )
    );
  }

  updateSettings(
    userId: string,
    settings: Record<Setting, string>,
    options?: RequestOptions
  ): Observable<User | undefined> {
    const path = `/api/user/${userId}/settings`;
    return this.http.patch<User>(path, settings, options).pipe(
      map(convertTemporals),
      catchError((err: HttpErrorResponse) =>
        this.errorHandler.handleError(err, {
          name: 'updateSettings',
          params: [userId, settings, options],
          method: 'patch',
          path
        })
      )
    );
  }
}
