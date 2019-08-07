import { Inject, Injectable, Optional } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { convertTemporals } from '../shared/mapper-utils';
import { Aggregate, PagedList, RequestCriteria, User } from '@dev/translatr-model';
import { AbstractService } from './abstract.service';
import { Router } from '@angular/router';
import { LOGIN_URL } from '@translatr/utils';

@Injectable({
  providedIn: 'root'
})
export class UserService extends AbstractService<User, RequestCriteria> {
  constructor(
    http: HttpClient,
    router?: Router,
    @Inject(LOGIN_URL) @Optional() loginUrl?: string
  ) {
    super(http, router, loginUrl, () => '/api/users', '/api/user');
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
      .get<User>(`/api/${username}`, options)
      .pipe(map(convertTemporals));
  }

  me(): Observable<User | undefined> {
    return this.http.get<User>('/api/me').pipe(map(convertTemporals));
  }

  activity(userId: string): Observable<PagedList<Aggregate> | undefined> {
    return this.http.get<PagedList<Aggregate>>(`/api/user/${userId}/activity`);
  }
}
