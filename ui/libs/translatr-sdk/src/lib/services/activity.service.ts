import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Activity, ActivityCriteria, Aggregate, PagedList } from '@dev/translatr-model';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { HttpHeader } from './http-header';
import { LanguageProvider } from './language-provider';

@Injectable({
  providedIn: 'root'
})
export class ActivityService {
  constructor(
    private readonly http: HttpClient,
    private readonly languageProvider: LanguageProvider
  ) {}

  find(criteria?: ActivityCriteria): Observable<PagedList<Activity>> {
    return this.http.get<PagedList<Activity>>('/api/activities', {
      params: criteria as { [param: string]: string | string[] }
    });
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
