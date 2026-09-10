import { HttpContext, HttpErrorResponse } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { RequestCriteria } from '@dev/translatr-model';
import { AbstractService, ResourceOperations } from './abstract.service';
import { ACCESS_TOKEN } from './access-token.interceptor';
import { ErrorHandler } from './error-handler';
import { LanguageProvider } from './language-provider';

interface Dto {
  id: string;
  whenCreated?: Date | string;
  whenUpdated?: Date | string;
}

const ISO = '2026-01-02T03:04:05.000Z';

function makeOps() {
  const ops = {
    entityPath: '/api/thing',
    listPath: jest.fn((_c?: RequestCriteria) => '/api/things'),
    list: jest.fn(
      (_c: RequestCriteria | undefined, _ctx?: HttpContext): Observable<unknown> =>
        of({
          list: [{ id: 'a', whenCreated: ISO, whenUpdated: ISO }],
          total: 1,
          offset: 0,
          limit: 20,
          hasNext: false,
          hasPrev: false,
        }),
    ),
    get: jest.fn(
      (_id: string | number, _ctx?: HttpContext): Observable<Dto> =>
        of({ id: 'a', whenCreated: ISO, whenUpdated: ISO }),
    ),
    create: jest.fn(
      (dto: Dto, _ctx?: HttpContext): Observable<Dto> => of({ ...dto, whenCreated: ISO }),
    ),
    update: jest.fn(
      (dto: Partial<Dto>, _ctx?: HttpContext): Observable<Dto> =>
        of({ id: 'a', ...dto, whenUpdated: ISO }),
    ),
    delete: jest.fn((_id: string | number, _ctx?: HttpContext): Observable<Dto> => of({ id: 'a' })),
  };
  return ops as unknown as ResourceOperations<Dto, RequestCriteria> & typeof ops;
}

class TestService extends AbstractService<Dto, RequestCriteria> {
  constructor(ops: ReturnType<typeof makeOps>, errorHandler: ErrorHandler) {
    super({} as never, errorHandler, new LanguageProvider(), ops);
  }
}

describe('AbstractService — delegation to the operations adapter', () => {
  let ops: ReturnType<typeof makeOps>;
  let errorHandler: ErrorHandler;
  let service: TestService;

  beforeEach(() => {
    ops = makeOps();
    errorHandler = new ErrorHandler();
    service = new TestService(ops, errorHandler);
  });

  it('find() delegates to operations.list and converts every item temporal to Date', () => {
    const criteria: RequestCriteria = { search: 'x', offset: 5, limit: 10 };
    let result: { list: Array<{ whenCreated?: unknown; whenUpdated?: unknown }> } | undefined;
    service
      .find(criteria)
      .subscribe({ next: (v) => (result = v as never), error: () => undefined });

    expect(ops.list).toHaveBeenCalledWith(criteria, undefined);
    expect(result?.list[0].whenCreated).toBeInstanceOf(Date);
    expect(result?.list[0].whenUpdated).toBeInstanceOf(Date);
  });

  it('get() delegates to operations.get and converts temporals to Date', () => {
    let result: { whenCreated?: unknown } | undefined;
    service.get('a').subscribe({ next: (v) => (result = v as never), error: () => undefined });

    expect(ops.get).toHaveBeenCalledWith('a', undefined);
    expect(result?.whenCreated).toBeInstanceOf(Date);
  });

  it('create() delegates to operations.create', () => {
    const dto: Dto = { id: 'a' };
    service.create(dto).subscribe({ error: () => undefined });
    expect(ops.create).toHaveBeenCalledWith(dto, undefined);
  });

  it('update() delegates to operations.update', () => {
    service.update({ id: 'a' }).subscribe({ error: () => undefined });
    expect(ops.update).toHaveBeenCalledWith({ id: 'a' }, undefined);
  });

  it('delete() delegates to operations.delete', () => {
    service.delete('a').subscribe({ error: () => undefined });
    expect(ops.delete).toHaveBeenCalledWith('a', undefined);
  });

  it('deleteAll() fans out one operations.delete per id', () => {
    service.deleteAll(['a', 'b', 'c']).subscribe({ error: () => undefined });
    expect(ops.delete).toHaveBeenCalledTimes(3);
    expect(ops.delete.mock.calls.map((c) => c[0])).toEqual(['a', 'b', 'c']);
  });

  it('routes a failed get() through ErrorHandler.handleError with request metadata', () => {
    const err = new HttpErrorResponse({ status: 500 });
    ops.get.mockReturnValueOnce(throwError(() => err));
    const handleError = jest
      .spyOn(errorHandler, 'handleError')
      .mockReturnValue(of({ id: 'handled' } as Dto));

    service.get('a').subscribe({ error: () => undefined });

    expect(handleError).toHaveBeenCalledTimes(1);
    const [passedErr, meta] = handleError.mock.calls[0];
    expect(passedErr).toBe(err);
    expect(meta).toMatchObject({ name: 'get', method: 'get', path: '/api/thing/a' });
  });
});

describe('AbstractService.withAuth', () => {
  let ops: ReturnType<typeof makeOps>;
  let service: TestService;

  beforeEach(() => {
    ops = makeOps();
    service = new TestService(ops, new ErrorHandler());
  });

  it('passes an HttpContext carrying the access token to the delegated call', () => {
    service
      .withAuth('tok-1')
      .create({ id: 'a' })
      .subscribe({ error: () => undefined });

    const context = ops.create.mock.calls[0][1] as HttpContext;
    expect(context).toBeInstanceOf(HttpContext);
    expect(context.get(ACCESS_TOKEN)).toBe('tok-1');
  });

  it('does not affect the original instance', () => {
    service.withAuth('tok-1');
    service.create({ id: 'a' }).subscribe({ error: () => undefined });

    expect(ops.create.mock.calls[0][1]).toBeUndefined();
  });

  it('scopes the token to the returned copy, not concurrent calls on the original', () => {
    const scoped = service.withAuth('tok-1');

    scoped.get('a').subscribe({ error: () => undefined });
    service.get('b').subscribe({ error: () => undefined });

    expect((ops.get.mock.calls[0][1] as HttpContext).get(ACCESS_TOKEN)).toBe('tok-1');
    expect(ops.get.mock.calls[1][1]).toBeUndefined();
  });
});
