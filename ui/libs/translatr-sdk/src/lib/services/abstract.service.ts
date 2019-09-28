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
    return this.http
      .get<PagedList<DTO>>(this.listPath(criteria), {
        params: { ...(criteria ? ((criteria as unknown) as object) : {}) }
      })
      .pipe(
        map((list: PagedList<DTO>) => ({
          ...list,
          list: convertTemporalsList(list.list)
        })),
        catchError((err: HttpErrorResponse) =>
          this.errorHandler.handleError(err))
      );
  }

  get(id: string | number, criteria?: CRITERIA): Observable<DTO> {
    const path = `${this.entityPath}/${id}`;
    return this.http
      .get<DTO>(path, {
        params: { ...(criteria ? ((criteria as unknown) as object) : {}) }
      })
      .pipe(
        map(convertTemporals),
        catchError((err: HttpErrorResponse) =>
          this.errorHandler.handleError(err))
      );
  }

  create(dto: DTO, options?: RequestOptions): Observable<DTO | undefined> {
    return this.http.post<DTO>(this.entityPath, dto, options).pipe(
      map(convertTemporals),
      catchError((err: HttpErrorResponse) =>
        this.errorHandler.handleError(err))
    );
  }

  update(dto: DTO, options?: RequestOptions): Observable<DTO | undefined> {
    return this.http.put<DTO>(this.entityPath, dto, options).pipe(
      map(convertTemporals),
      catchError((err: HttpErrorResponse) =>
        this.errorHandler.handleError(err))
    );
  }

  delete(
    id: string | number,
    options?: RequestOptions
  ): Observable<DTO | undefined> {
    const path = `${this.entityPath}/${id}`;
    return this.http.delete<DTO>(path, options).pipe(
      map(convertTemporals),
      catchError((err: HttpErrorResponse) =>
        this.errorHandler.handleError(err))
    );
  }

  deleteAll(ids: (string | number)[]): Observable<DTO[]> {
    return combineLatest(ids.map((id: string | number) => this.delete(id)));
  }
}
