import { HttpClient, HttpContext, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import {
  Aggregate,
  PagedList,
  Profile,
  RequestCriteria,
  Setting,
  User,
} from '@dev/translatr-model';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { convertTemporals } from '../shared/mapper-utils';
import { UsersService } from '../generated/api/users.service';
import { UserDto } from '../generated/model/userDto';
import {
  AbstractService,
  PagedListLike,
  RequestOptions,
  encodePathParam,
} from './abstract.service';
import { ErrorHandler } from './error-handler';
import { LanguageProvider } from './language-provider';

@Injectable({
  providedIn: 'root',
})
export class UserService extends AbstractService<User, RequestCriteria> {
  constructor(
    http: HttpClient,
    errorHandler: ErrorHandler,
    languageProvider: LanguageProvider,
    client: UsersService,
  ) {
    super(http, errorHandler, languageProvider, {
      entityPath: '/api/user',
      listPath: () => '/api/users',
      list: (c: RequestCriteria | undefined, context?: HttpContext) =>
        client.findUsers(
          c?.search,
          c?.offset,
          c?.limit,
          c?.order,
          c?.fetch,
          undefined,
          undefined,
          'body',
          false,
          { context },
        ) as unknown as Observable<PagedListLike<User>>,
      get: (id: string | number, context?: HttpContext) =>
        client.getUser(String(id), 'body', false, { context }) as unknown as Observable<User>,
      // No user-creation operation exists in the OpenAPI contract, and adding one
      // is out of scope for #282. See design.md Decision 8.
      create: () =>
        throwError(
          () =>
            new Error(
              'User creation is not available through the SDK transport (no contract operation) — see #282',
            ),
        ),
      update: (dto: Partial<User>, context?: HttpContext) =>
        client.updateUser(dto as unknown as UserDto, 'body', false, {
          context,
        }) as unknown as Observable<User>,
      delete: (id: string | number, context?: HttpContext) =>
        client.deleteUser(String(id), 'body', false, { context }) as unknown as Observable<User>,
    });
  }

  byUsername(
    username: string,
    options?: {
      params?:
        | HttpParams
        | {
            [param: string]: string | string[];
          };
    },
  ): Observable<User | undefined> {
    const path = `/api/${encodePathParam(username)}`;
    return this.http
      .get<User>(path, {
        context: this.authContext,
        ...options,
      })
      .pipe(
        map(convertTemporals),
        catchError((err: HttpErrorResponse) =>
          this.errorHandler.handleError(err, {
            name: 'byUsername',
            params: [username, options],
            method: 'get',
            path,
          }),
        ),
      );
  }

  me(params: Record<string, string> = {}): Observable<User | undefined> {
    const path = '/api/me';
    return this.http
      .get<User>(path, {
        context: this.authContext,
        params,
      })
      .pipe(
        map(convertTemporals),
        catchError((err: HttpErrorResponse) =>
          this.errorHandler.handleError(err, {
            name: 'me',
            params: [params],
            method: 'get',
            path,
          }),
        ),
      );
  }

  activity(userId: string): Observable<PagedList<Aggregate> | undefined> {
    const path = `/api/user/${userId}/activity`;
    return this.http
      .get<PagedList<Aggregate>>(path, {
        context: this.authContext,
      })
      .pipe(
        catchError((err: HttpErrorResponse) =>
          this.errorHandler.handleError(err, {
            name: 'activity',
            params: [userId],
            method: 'get',
            path,
          }),
        ),
      );
  }

  updateSettings(
    userId: string,
    settings: Record<Setting, string>,
    options?: RequestOptions,
  ): Observable<User | undefined> {
    const path = `/api/user/${userId}/settings`;
    return this.http
      .patch<User>(path, settings, {
        context: this.authContext,
        ...options,
      })
      .pipe(
        map(convertTemporals),
        catchError((err: HttpErrorResponse) =>
          this.errorHandler.handleError(err, {
            name: 'updateSettings',
            params: [userId, settings, options],
            method: 'patch',
            path,
          }),
        ),
      );
  }

  authProfile(): Observable<Profile | undefined> {
    const path = '/api/profile';
    return this.http.get<Profile>(path, { context: this.authContext }).pipe(
      map(convertTemporals),
      catchError((err: HttpErrorResponse) =>
        this.errorHandler.handleError(err, {
          name: 'authProfile',
          params: [],
          method: 'get',
          path,
        }),
      ),
    );
  }
}
