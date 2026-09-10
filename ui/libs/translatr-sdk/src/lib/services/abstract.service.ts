import { HttpClient, HttpContext, HttpErrorResponse } from '@angular/common/http';
import { PagedList, RequestCriteria } from '@dev/translatr-model';
import { combineLatest, Observable } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { convertTemporals, convertTemporalsList } from '../shared';
import { ACCESS_TOKEN } from './access-token.interceptor';
import { ErrorHandler } from './error-handler';
import { LanguageProvider } from './language-provider';

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

/**
 * Structural shape shared by `PagedList<T>` and the generated `Paged<X>List`
 * wrappers, so the operations adapter does not depend on a generated class name.
 */
export interface PagedListLike<T> {
  list: T[];
  total?: number;
  offset: number;
  limit: number;
  hasNext: boolean;
  hasPrev: boolean;
}

/**
 * Per-resource seam between `AbstractService`'s generic verbs and one generated
 * API client. Each resource service builds this inline from its injected client.
 * `entityPath` / `listPath` feed `ErrorHandler` metadata only; every verb takes
 * an optional `HttpContext` that `AbstractService` supplies to carry a
 * `withAuth` access token.
 */
export interface ResourceOperations<DTO, CRITERIA extends RequestCriteria> {
  entityPath: string;
  listPath(criteria?: CRITERIA): string;
  list(criteria: CRITERIA | undefined, context?: HttpContext): Observable<PagedListLike<DTO>>;
  get(id: string | number, context?: HttpContext): Observable<DTO>;
  create(dto: DTO, context?: HttpContext): Observable<DTO>;
  update(dto: Partial<DTO>, context?: HttpContext): Observable<DTO>;
  delete(id: string | number, context?: HttpContext): Observable<DTO>;
}

export class AbstractService<DTO, CRITERIA extends RequestCriteria> {
  /**
   * Set only on the copies returned by `withAuth`; `undefined` on the shared,
   * DI-provided instance so its requests stay unauthenticated.
   */
  protected authContext?: HttpContext;

  constructor(
    protected readonly http: HttpClient,
    protected readonly errorHandler: ErrorHandler,
    protected readonly languageProvider: LanguageProvider,
    private readonly operations: ResourceOperations<DTO, CRITERIA>,
  ) {
    if (errorHandler === undefined) {
      this.errorHandler = new ErrorHandler();
    }
  }

  /**
   * Returns a copy of this service whose requests carry `accessToken` as an
   * `?access_token=` query parameter (via `AccessTokenInterceptor`). The shared
   * instance is not mutated.
   */
  withAuth(accessToken: string): this {
    const copy: this = Object.create(this);
    (copy as AbstractService<DTO, CRITERIA>).authContext = new HttpContext().set(
      ACCESS_TOKEN,
      accessToken,
    );
    return copy;
  }

  find(criteria?: CRITERIA): Observable<PagedList<DTO> | undefined> {
    const path = this.operations.listPath(criteria);
    return this.operations.list(criteria, this.authContext).pipe(
      map((list: PagedListLike<DTO>) => ({
        ...list,
        list: convertTemporalsList(list.list),
      })),
      catchError((err: HttpErrorResponse) =>
        this.errorHandler.handleError(err, {
          name: 'find',
          params: [criteria],
          method: 'get',
          path,
        }),
      ),
    ) as Observable<PagedList<DTO> | undefined>;
  }

  get(id: string | number, criteria?: CRITERIA): Observable<DTO> {
    const path = `${this.operations.entityPath}/${encodePathParam(id)}`;
    return this.operations.get(id, this.authContext).pipe(
      map(convertTemporals),
      catchError((err: HttpErrorResponse) =>
        this.errorHandler.handleError(err, {
          name: 'get',
          params: [id, criteria],
          method: 'get',
          path,
        }),
      ),
    );
  }

  create(dto: DTO): Observable<DTO | undefined> {
    return this.operations.create(dto, this.authContext).pipe(
      map(convertTemporals),
      catchError((err: HttpErrorResponse) =>
        this.errorHandler.handleError(err, {
          name: 'create',
          params: [dto],
          method: 'post',
          path: this.operations.entityPath,
        }),
      ),
    );
  }

  update(dto: Partial<DTO>): Observable<DTO | undefined> {
    return this.operations.update(dto, this.authContext).pipe(
      map(convertTemporals),
      catchError((err: HttpErrorResponse) =>
        this.errorHandler.handleError(err, {
          name: 'update',
          params: [dto],
          method: 'put',
          path: this.operations.entityPath,
        }),
      ),
    );
  }

  delete(id: string | number): Observable<DTO | undefined> {
    const path = `${this.operations.entityPath}/${encodePathParam(id)}`;
    return this.operations.delete(id, this.authContext).pipe(
      map(convertTemporals),
      catchError((err: HttpErrorResponse) =>
        this.errorHandler.handleError(err, {
          name: 'delete',
          params: [id],
          method: 'delete',
          path,
        }),
      ),
    );
  }

  deleteAll(ids: (string | number)[]): Observable<DTO[]> {
    return combineLatest(ids.map((id: string | number) => this.delete(id)));
  }
}
