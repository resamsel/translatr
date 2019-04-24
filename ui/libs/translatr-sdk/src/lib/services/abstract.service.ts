import {HttpClient} from "@angular/common/http";
import {convertTemporals, convertTemporalsList} from '../shared';
import {combineLatest, Observable} from "rxjs";
import {map} from "rxjs/operators";
import {PagedList, RequestCriteria} from "@dev/translatr-model";

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
      .get<PagedList<DTO>>(this.listPath, {params: {...criteria ? (criteria as object) : {}}})
      .pipe(map((list: PagedList<DTO>) => ({
        ...list,
        list: convertTemporalsList(list.list)
      })));
  }

  create(dto: DTO, options?: RequestOptions): Observable<DTO | undefined> {
    return this.http
      .post<DTO>(this.entityPath, dto, options)
      .pipe(map(convertTemporals));
  }

  update(dto: DTO, options?: RequestOptions): Observable<DTO | undefined> {
    return this.http
      .put<DTO>(this.entityPath, dto, options)
      .pipe(map(convertTemporals));
  }

  delete(id: string | number, options?: RequestOptions): Observable<DTO | undefined> {
    return this.http
      .delete<DTO>(`${this.entityPath}/${id}`, options)
      .pipe(map(convertTemporals));
  }

  deleteAll(ids: (string | number)[]): Observable<DTO[]> {
    return combineLatest(...ids.map((id: string | number) => this.delete(id)));
  }
}

