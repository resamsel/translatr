import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from "@angular/common/http";
import { Observable } from "rxjs";
import { map } from "rxjs/operators";
import { convertTemporals, convertTemporalsList } from "../shared/mapper-utils";
import { Aggregate, PagedList, RequestCriteria, User } from "@dev/translatr-model";
import { combineLatest } from "rxjs/internal/observable/combineLatest";

@Injectable({
  providedIn: 'root'
})
export class UserService {

  constructor(private readonly http: HttpClient) {
  }

  getUsers(criteria?: RequestCriteria): Observable<PagedList<User> | undefined> {
    return this.http
      .get<PagedList<User>>('/api/users', {params: {...criteria ? criteria : {}}})
      .pipe(map((list: PagedList<User>) => ({
        ...list,
        list: convertTemporalsList(list.list)
      })));
  }

  get(userId: string, criteria?: RequestCriteria): Observable<User> {
    return this.http
      .get<User>(`/api/user/${userId}`, {params: {...criteria}})
      .pipe(map(convertTemporals));
  }

  getUserByName(username: string, options?: {
    params?: HttpParams | {
      [param: string]: string | string[];
    }
  }): Observable<User | undefined> {
    return this.http
      .get<User>(`/api/${username}`, options)
      .pipe(map(convertTemporals));
  }

  getLoggedInUser(): Observable<User | undefined> {
    return this.http
      .get<User>('/api/me')
      .pipe(map(convertTemporals));
  }

  activity(userId: string): Observable<PagedList<Aggregate> | undefined> {
    return this.http
      .get<PagedList<Aggregate>>(`/api/user/${userId}/activity`);
  }

  create(user: User): Observable<User | undefined> {
    return this.http
      .post<User>('/api/user', user)
      .pipe(map(convertTemporals));
  }

  update(user: User): Observable<User | undefined> {
    return this.http
      .put<User>('/api/user', user)
      .pipe(map(convertTemporals));
  }

  delete(userId: string): Observable<User | undefined> {
    return this.http
      .delete<User>(`/api/user/${userId}`)
      .pipe(map(convertTemporals));
  }

  deleteAll(userIds: string[]): Observable<User[]> {
    return combineLatest(...userIds.map((id: string) => this.delete(id)));
  }
}
