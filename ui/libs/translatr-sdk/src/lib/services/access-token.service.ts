import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {
  AccessToken,
  convertTemporals,
  convertTemporalsList,
  PagedList,
  RequestCriteria,
  User
} from "@dev/translatr-sdk";
import {Observable} from "rxjs";
import {map} from "rxjs/operators";

export interface AccessTokenCriteria extends RequestCriteria {
  userId?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AccessTokenService {

  constructor(private readonly http: HttpClient) {
  }

  find(criteria?: AccessTokenCriteria): Observable<PagedList<AccessToken>> {
    return this.http
      .get<PagedList<AccessToken>>('/api/accesstokens', {params: {...criteria || {}}})
      .pipe(map((list: PagedList<AccessToken>) => ({
        ...list,
        list: convertTemporalsList(list.list)
      })));
  }

  create(accessToken: AccessToken): Observable<AccessToken | undefined> {
    return this.http
      .post<AccessToken>('/api/accesstoken', accessToken)
      .pipe(map(convertTemporals));
  }

  update(accessToken: AccessToken): Observable<AccessToken | undefined> {
    return this.http
      .put<AccessToken>('/api/accesstoken', accessToken)
      .pipe(map(convertTemporals));
  }

  delete(accessTokenId: string): Observable<AccessToken | undefined> {
    return this.http
      .delete<AccessToken>(`/api/accesstoken/${accessTokenId}`)
      .pipe(map(convertTemporals));
  }
}
