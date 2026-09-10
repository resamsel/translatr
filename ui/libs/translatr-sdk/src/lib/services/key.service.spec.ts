import { HttpClient } from '@angular/common/http';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ErrorHandler, LanguageProvider } from '@dev/translatr-sdk';
import { of } from 'rxjs';

import { KeyService } from './key.service';
import { KeysService } from '../generated/api/keys.service';

describe('KeyService', () => {
  beforeEach(() =>
    TestBed.configureTestingModule({
      providers: [{ provide: HttpClient, useFactory: () => ({}) }, ErrorHandler, LanguageProvider],
    }),
  );

  it('should be created', () => {
    const service: KeyService = TestBed.inject(KeyService);
    expect(service).toBeTruthy();
  });
});

describe('KeyService — generated client delegation', () => {
  let service: KeyService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ErrorHandler, LanguageProvider],
    });
    service = TestBed.inject(KeyService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.match(() => true).forEach((r) => !r.cancelled && r.flush({})));

  it('find() calls KeysService.findKeysByProject with projectId + criteria mapped positionally', () => {
    const spy = jest
      .spyOn(KeysService.prototype, 'findKeysByProject')
      .mockReturnValue(
        of({ list: [], total: 0, offset: 0, limit: 20, hasNext: false, hasPrev: false }) as never,
      );

    service
      .find({
        projectId: 'pr1',
        search: 's',
        offset: 5,
        limit: 10,
        order: 'name',
        fetch: 'f',
        localeId: 'l1',
        missing: true,
      })
      .subscribe({ error: () => undefined });

    // findKeysByProject(projectId, search, offset, limit, order, fetch, localeId, missing)
    expect(spy.mock.calls[0].slice(0, 8)).toEqual(['pr1', 's', 5, 10, 'name', 'f', 'l1', true]);
  });

  it('get() calls KeysService.getKey with the id', () => {
    const spy = jest
      .spyOn(KeysService.prototype, 'getKey')
      .mockReturnValue(of({ id: 'k1' }) as never);
    service.get('k1').subscribe({ error: () => undefined });
    expect(spy.mock.calls[0][0]).toBe('k1');
  });

  it('create() calls KeysService.createKey with the dto', () => {
    const spy = jest
      .spyOn(KeysService.prototype, 'createKey')
      .mockReturnValue(of({ id: 'k1' }) as never);
    const dto = { name: 'key', projectId: 'pr1' } as never;
    service.create(dto).subscribe({ error: () => undefined });
    expect(spy.mock.calls[0][0]).toEqual(dto);
  });

  it('update() calls KeysService.updateKey with the dto', () => {
    const spy = jest
      .spyOn(KeysService.prototype, 'updateKey')
      .mockReturnValue(of({ id: 'k1' }) as never);
    const dto = { id: 'k1', name: 'key2' } as never;
    service.update(dto).subscribe({ error: () => undefined });
    expect(spy.mock.calls[0][0]).toEqual(dto);
  });

  it('delete() calls KeysService.deleteKey with the id', () => {
    const spy = jest
      .spyOn(KeysService.prototype, 'deleteKey')
      .mockReturnValue(of({ id: 'k1' }) as never);
    service.delete('k1').subscribe({ error: () => undefined });
    expect(spy.mock.calls[0][0]).toBe('k1');
  });
});
