import { HttpClient } from '@angular/common/http';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ErrorHandler, LanguageProvider } from '@dev/translatr-sdk';
import { of } from 'rxjs';

import { LocaleService } from './locale.service';
import { LocalesService } from '../generated/api/locales.service';

describe('LocaleService', () => {
  beforeEach(() =>
    TestBed.configureTestingModule({
      providers: [{ provide: HttpClient, useFactory: () => ({}) }, ErrorHandler, LanguageProvider],
    }),
  );

  it('should be created', () => {
    const service: LocaleService = TestBed.inject(LocaleService);
    expect(service).toBeTruthy();
  });
});

describe('LocaleService — generated client delegation', () => {
  let service: LocaleService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ErrorHandler, LanguageProvider],
    });
    service = TestBed.inject(LocaleService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.match(() => true).forEach((r) => !r.cancelled && r.flush({})));

  it('find() calls LocalesService.findLocalesByProject with projectId + criteria mapped positionally', () => {
    const spy = jest
      .spyOn(LocalesService.prototype, 'findLocalesByProject')
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
        keyId: 'k1',
        missing: true,
      })
      .subscribe({ error: () => undefined });

    // findLocalesByProject(projectId, search, offset, limit, order, fetch, keyId, missing, localeName)
    expect(spy).toHaveBeenCalled();
    expect(spy.mock.calls[0].slice(0, 8)).toEqual(['pr1', 's', 5, 10, 'name', 'f', 'k1', true]);
  });

  it('get() calls LocalesService.getLocale with the id', () => {
    const spy = jest
      .spyOn(LocalesService.prototype, 'getLocale')
      .mockReturnValue(of({ id: 'l1' }) as never);
    service.get('l1').subscribe({ error: () => undefined });
    expect(spy.mock.calls[0][0]).toBe('l1');
  });

  it('create() calls LocalesService.createLocale with the dto', () => {
    const spy = jest
      .spyOn(LocalesService.prototype, 'createLocale')
      .mockReturnValue(of({ id: 'l1' }) as never);
    const dto = { name: 'de', projectId: 'pr1' } as never;
    service.create(dto).subscribe({ error: () => undefined });
    expect(spy.mock.calls[0][0]).toEqual(dto);
  });

  it('update() calls LocalesService.updateLocale with the dto', () => {
    const spy = jest
      .spyOn(LocalesService.prototype, 'updateLocale')
      .mockReturnValue(of({ id: 'l1' }) as never);
    const dto = { id: 'l1', name: 'de-DE' } as never;
    service.update(dto).subscribe({ error: () => undefined });
    expect(spy.mock.calls[0][0]).toEqual(dto);
  });

  it('delete() calls LocalesService.deleteLocale with the id', () => {
    const spy = jest
      .spyOn(LocalesService.prototype, 'deleteLocale')
      .mockReturnValue(of({ id: 'l1' }) as never);
    service.delete('l1').subscribe({ error: () => undefined });
    expect(spy.mock.calls[0][0]).toBe('l1');
  });
});
