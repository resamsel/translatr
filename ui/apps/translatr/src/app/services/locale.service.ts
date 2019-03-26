import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {Locale} from "../shared/locale";
import {Observable} from "rxjs";
import {map} from "rxjs/operators";
import {convertTemporals, convertTemporalsList} from "../shared/mapper-utils";
import {PagedList} from "../shared/paged-list";

@Injectable({
  providedIn: 'root'
})
export class LocaleService {

  constructor(private readonly http: HttpClient) {
  }

  byOwnerAndProjectNameAndName(
    options: {
      username: string;
      projectName: string;
      localeName: string;
      params?: HttpParams | {
        [param: string]: string | string[];
      }
    }): Observable<Locale> {
    return this.http
      .get<Locale>(
        `/api/${options.username}/${options.projectName}/locales/${options.localeName}`,
        {params: options.params}
      )
      .pipe(map((locale: Locale) => convertTemporals(locale)));
  }

  getLocales(
    options: {
      projectId: string;
      options?: {
        params?: HttpParams | {
          [param: string]: string | string[];
        };
      }
    }): Observable<PagedList<Locale>> {
    return this.http
      .get<PagedList<Locale>>(`/api/locales/${options.projectId}`, options.options)
      .pipe(
        map((payload: PagedList<Locale>) => ({
          ...payload,
          list: convertTemporalsList(payload.list)
        }))
      );
  }
}
