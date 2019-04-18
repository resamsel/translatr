import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {Observable} from "rxjs";
import {map} from "rxjs/operators";
import {convertTemporals, convertTemporalsList} from "../shared/mapper-utils";
import {AbstractService} from "@dev/translatr-sdk/src/lib/services/abstract.service";
import {Locale, PagedList, RequestCriteria} from "@dev/translatr-model";

@Injectable({
  providedIn: 'root'
})
export class LocaleService extends AbstractService<Locale, RequestCriteria> {

  constructor(http: HttpClient) {
    super(http, '/api/locales', '/api/locale');
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
