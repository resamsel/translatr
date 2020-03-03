import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { convertTemporals, convertTemporalsList } from '../shared';
import { combineLatest, Observable } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { PagedList, RequestCriteria } from '@dev/translatr-model';
import { ErrorHandler } from './error-handler';

export interface RequestOptions {
  params: {
    [param: string]: string | string[];
  };
}

export const encodePathParam = (param: string | number): string | number => {
  if (typeof param === 'string') {
    return encodeURIComponent(param);
  }

  return param;
};

export class AbstractService<DTO, CRITERIA extends RequestCriteria> {
  constructor(
    protected readonly http: HttpClient,
    protected readonly errorHandler: ErrorHandler,
    private readonly listPath: (criteria: CRITERIA) => string,
    private readonly entityPath: string
  ) {
    if (errorHandler === undefined) {
      this.errorHandler = new ErrorHandler();
    }
  }

  find(criteria?: CRITERIA): Observable<PagedList<DTO> | undefined> {
    const path = this.listPath(criteria);
    return this.http
      .get<PagedList<DTO>>(path, {
        params: { ...(criteria ? ((criteria as unknown) as object) : {}) }
      })
      .pipe(
        map((list: PagedList<DTO>) => ({
          ...list,
          list: convertTemporalsList(list.list)
        })),
        catchError((err: HttpErrorResponse) =>
          this.errorHandler.handleError(err, {
            name: 'find',
            params: [criteria],
            method: 'get',
            path
          }))
      );
  }

  get(id: string | number, criteria?: CRITERIA): Observable<DTO> {
    const path = `${this.entityPath}/${encodePathParam(id)}`;
    return this.http
      .get<DTO>(path, {
        params: { ...(criteria ? ((criteria as unknown) as object) : {}) }
      })
      .pipe(
        map(convertTemporals),
        catchError((err: HttpErrorResponse) =>
          this.errorHandler.handleError(err, {
            name: 'get',
            params: [id, criteria],
            method: 'get',
            path
          }))
      );
  }

  create(dto: DTO, options?: RequestOptions): Observable<DTO | undefined> {
    return this.http.post<DTO>(this.entityPath, dto, options).pipe(
      map(convertTemporals),
      catchError((err: HttpErrorResponse) =>
        this.errorHandler.handleError(err, {
          name: 'create',
          params: [dto, options],
          method: 'post',
          path: this.entityPath
        }))
    );
  }

  update(dto: DTO, options?: RequestOptions): Observable<DTO | undefined> {
    return this.http.put<DTO>(this.entityPath, dto, options).pipe(
      map(convertTemporals),
      catchError((err: HttpErrorResponse) =>
        this.errorHandler.handleError(err, {
          name: 'update',
          params: [dto, options],
          method: 'put',
          path: this.entityPath
        }))
    );
  }

  delete(
    id: string | number,
    options?: RequestOptions
  ): Observable<DTO | undefined> {
    const path = `${this.entityPath}/${encodePathParam(id)}`;
    return this.http.delete<DTO>(path, options).pipe(
      map(convertTemporals),
      catchError((err: HttpErrorResponse) =>
        this.errorHandler.handleError(err, {
          name: 'delete',
          params: [id, options],
          method: 'delete',
          path
        }))
    );
  }

  deleteAll(ids: (string | number)[]): Observable<DTO[]> {
    return combineLatest(ids.map((id: string | number) => this.delete(id)));
  }
}
