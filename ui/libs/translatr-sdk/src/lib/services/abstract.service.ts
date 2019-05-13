import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { convertTemporals, convertTemporalsList } from '../shared';
import { combineLatest, Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { PagedList, RequestCriteria } from '@dev/translatr-model';
import { Router } from '@angular/router';
import { environment } from '../../../../../apps/translatr/src/environments/environment';

export interface RequestOptions {
  params: {
    [param: string]: string | string[];
  };
}

export class AbstractService<DTO, CRITERIA extends RequestCriteria> {
  constructor(
    protected readonly http: HttpClient,
    protected readonly router: Router,
    private readonly listPath: (criteria: CRITERIA) => string,
    private readonly entityPath: string
  ) {}

  find(criteria?: CRITERIA): Observable<PagedList<DTO> | undefined> {
    return this.http
      .get<PagedList<DTO>>(this.listPath(criteria), {
        params: { ...(criteria ? ((criteria as unknown) as object) : {}) }
      })
      .pipe(
        map((list: PagedList<DTO>) => ({
          ...list,
          list: convertTemporalsList(list.list)
        })),
        catchError((err: HttpErrorResponse) => {
          console.error(
            `Error while finding ${this.listPath(criteria)}`,
            err,
            criteria
          );

          if (err.status >= 400) {
            console.log('navigateByUrl', `${environment.endpointUrl}/login`);
            window.location.href = `${environment.endpointUrl}/login?redirect_uri=${this.router.url}`;
          }

          return throwError(err);
        })
      );
  }

  get(id: string, criteria?: CRITERIA): Observable<DTO> {
    const path = `${this.entityPath}/${id}`;
    return this.http
      .get<DTO>(path, {
        params: { ...(criteria ? ((criteria as unknown) as object) : {}) }
      })
      .pipe(
        map(convertTemporals),
        catchError(err => {
          console.error(`Error while getting ${path}`, err, id, criteria);
          return throwError(err);
        })
      );
  }

  create(dto: DTO, options?: RequestOptions): Observable<DTO | undefined> {
    return this.http.post<DTO>(this.entityPath, dto, options).pipe(
      map(convertTemporals),
      catchError(err => {
        console.error(
          `Error while creating ${this.entityPath}`,
          err,
          dto,
          options
        );
        return throwError(err);
      })
    );
  }

  update(dto: DTO, options?: RequestOptions): Observable<DTO | undefined> {
    return this.http.put<DTO>(this.entityPath, dto, options).pipe(
      map(convertTemporals),
      catchError(err => {
        console.error(
          `Error while updating ${this.entityPath}`,
          err,
          dto,
          options
        );
        return throwError(err);
      })
    );
  }

  delete(
    id: string | number,
    options?: RequestOptions
  ): Observable<DTO | undefined> {
    const path = `${this.entityPath}/${id}`;
    return this.http.delete<DTO>(path, options).pipe(
      map(convertTemporals),
      catchError(err => {
        console.error(`Error while deleting ${path}`, err, id, options);
        return throwError(err);
      })
    );
  }

  deleteAll(ids: (string | number)[]): Observable<DTO[]> {
    return combineLatest(ids.map((id: string | number) => this.delete(id)));
  }
}
