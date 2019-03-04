import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {Observable} from "rxjs";
import {map} from "rxjs/operators";
import {User} from "../shared/user";

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
      .pipe(
        map((user: User) => ({
          ...user,
          whenCreated: new Date(user.whenCreated),
          whenUpdated: new Date(user.whenUpdated)
        }))
      );
  }
}
