import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {Observable} from "rxjs";
import {map} from "rxjs/operators";
import {convertTemporals, convertTemporalsList} from "../shared/mapper-utils";
import {Key} from "../shared/key";
import {PagedList} from "../shared/paged-list";
import {RequestCriteria} from "../shared/request-criteria";

@Injectable({
  providedIn: 'root'
})
export class KeyService {

  constructor(private readonly http: HttpClient) {
  }

  byOwnerAndProjectNameAndName(
    options: {
      username: string;
      projectName: string;
      keyName: string;
      params?: HttpParams | {
        [param: string]: string | string[];
      }
    }): Observable<Key> {
    return this.http
      .get<Key>(`/api/${options.username}/${options.projectName}/keys/${options.keyName}`, options)
      .pipe(map(convertTemporals));
  }

  getKeys(options: {
    projectId: string;
    options?: {
      params?: HttpParams | RequestCriteria;
    }
  }): Observable<PagedList<Key>> {
    return this.http
      .get<PagedList<Key>>(`/api/keys/${options.projectId}`, {
        params: {
          ...options.options && options.options.params ? options.options.params : {}
        }
      })
      .pipe(
        map((list: PagedList<Key>) => ({
          ...list,
          list: convertTemporalsList(list.list)
        }))
      );
  }
}
