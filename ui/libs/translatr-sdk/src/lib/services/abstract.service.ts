import { HttpClient } from '@angular/common/http';
import { convertTemporals, convertTemporalsList } from '../shared';
import { combineLatest, Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { PagedList, RequestCriteria } from '@dev/translatr-model';

export interface RequestOptions {
  params: {
    [param: string]: string | string[];
  };
}

export class AbstractService<DTO, CRITERIA extends RequestCriteria> {
  constructor(
    protected readonly http: HttpClient,
    private readonly listPath: string,
    private readonly entityPath: string
  ) {
  }

  find(criteria?: CRITERIA): Observable<PagedList<DTO> | undefined> {
    return this.http
      .get<PagedList<DTO>>(this.listPath, {params: {...criteria ? (criteria as unknown as object) : {}}})
      .pipe(
        map((list: PagedList<DTO>) => ({
          ...list,
          list: convertTemporalsList(list.list)
        })),
        catchError(err => {
          console.error(`Error while finding ${this.listPath}`, err, criteria);
          return throwError(err);
        })
      );
  }

  create(dto: DTO, options?: RequestOptions): Observable<DTO | undefined> {
    return this.http
      .post<DTO>(this.entityPath, dto, options)
      .pipe(
        map(convertTemporals),
        catchError(err => {
          console.error(`Error while creating ${this.entityPath}`, err, dto, options);
          return throwError(err);
        })
      );
  }

  update(dto: DTO, options?: RequestOptions): Observable<DTO | undefined> {
    return this.http
      .put<DTO>(this.entityPath, dto, options)
      .pipe(
        map(convertTemporals),
        catchError(err => {
          console.error(`Error while updating ${this.entityPath}`, err, dto, options);
          return throwError(err);
        })
      );
  }

  delete(id: string | number, options?: RequestOptions): Observable<DTO | undefined> {
    const path = `${this.entityPath}/${id}`;
    return this.http
      .delete<DTO>(path, options)
      .pipe(
        map(convertTemporals),
        catchError(err => {
          console.error(`Error while deleting ${path}`, err, id, options);
          return throwError(err);
        })
      );
  }

  deleteAll(ids: (string | number)[]): Observable<DTO[]> {
    return combineLatest(...ids.map((id: string | number) => this.delete(id)));
  }
}

