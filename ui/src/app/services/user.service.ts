import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from "@angular/common/http";
import { Observable } from "rxjs";
import { map } from "rxjs/operators";
import { User } from "../shared/user";
import { convertTemporals } from "../shared/mapper-utils";
import { Aggregate } from "../shared/aggregate";
import { PagedList } from "../shared/paged-list";

@Injectable({
  providedIn: 'root'
})
export class UserService {

  constructor(private readonly http: HttpClient) {
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
}
