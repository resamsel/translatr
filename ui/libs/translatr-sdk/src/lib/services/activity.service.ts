import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Activity, ActivityCriteria, Aggregate, PagedList } from '@dev/translatr-model';
import { ErrorHandler } from './error-handler';
import { Observable } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { HttpHeader } from './http-header';
import { LanguageProvider } from './language-provider';
import { convertTemporalsList } from '../shared';

@Injectable({
  providedIn: 'root'
})
export class ActivityService {
  constructor(
    private readonly http: HttpClient,
    private readonly errorHandler: ErrorHandler,
    private readonly languageProvider: LanguageProvider
  ) {}

  find(criteria?: ActivityCriteria): Observable<PagedList<Activity>> {
    const path = '/api/activities';
    return this.http
      .get<PagedList<Activity>>(path, {
        params: criteria as { [param: string]: string | string[] }
      })
      .pipe(
        map((list: PagedList<Activity>) => ({
          ...list,
          list: convertTemporalsList(list.list)
        })),
        catchError((err: HttpErrorResponse) =>
          this.errorHandler.handleError(err, {
            name: 'find',
            params: [criteria],
            method: 'get',
            path
          })
        )
      );
  }

  aggregated(criteria: ActivityCriteria): Observable<PagedList<Aggregate>> {
    return this.http
      .get<PagedList<Aggregate>>('/api/activities/aggregated', {
        headers: {
          [HttpHeader.AcceptLanguage]: this.languageProvider.getActiveLang()
        },
        params: criteria as { [param: string]: string | string[] }
      })
      .pipe(
        map((list: PagedList<Aggregate>) => ({
          ...list,
          list: list.list.map((aggregate: Aggregate) => ({
            ...aggregate,
            date: new Date(aggregate.date)
          }))
        }))
      );
  }
}
