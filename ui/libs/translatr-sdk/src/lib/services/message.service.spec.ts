import { HttpClient } from '@angular/common/http';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ErrorHandler, LanguageProvider } from '@dev/translatr-sdk';
import { of } from 'rxjs';

import { MessageService } from './message.service';
import { MessagesService } from '../generated/api/messages.service';

describe('MessageService', () => {
  beforeEach(() =>
    TestBed.configureTestingModule({
      providers: [{ provide: HttpClient, useFactory: () => ({}) }, ErrorHandler, LanguageProvider],
    }),
  );

  it('should be created', () => {
    const service: MessageService = TestBed.inject(MessageService);
    expect(service).toBeTruthy();
  });
});

describe('MessageService — generated client delegation', () => {
  let service: MessageService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ErrorHandler, LanguageProvider],
    });
    service = TestBed.inject(MessageService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.match(() => true).forEach((r) => !r.cancelled && r.flush({})));

  it('find() calls MessagesService.findMessagesByProject with projectId + criteria mapped positionally', () => {
    const spy = jest
      .spyOn(MessagesService.prototype, 'findMessagesByProject')
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
        localeIds: 'l1,l2',
        keyIds: 'k1,k2',
        keyName: 'greeting',
      })
      .subscribe({ error: () => undefined });

    // findMessagesByProject(projectId, search, offset, limit, order, fetch, localeId, localeIds, keyId, keyIds, keyName)
    expect(spy).toHaveBeenCalled();
    const args = spy.mock.calls[0];
    expect(args.slice(0, 8)).toEqual(['pr1', 's', 5, 10, 'name', 'f', 'l1', 'l1,l2']);
    expect(args[9]).toBe('k1,k2'); // keyIds
    expect(args[10]).toBe('greeting'); // keyName
  });

  it('get() calls MessagesService.getMessage with the id', () => {
    const spy = jest
      .spyOn(MessagesService.prototype, 'getMessage')
      .mockReturnValue(of({ id: 'msg1' }) as never);
    service.get('msg1').subscribe({ error: () => undefined });
    expect(spy.mock.calls[0][0]).toBe('msg1');
  });

  it('create() calls MessagesService.createMessage with the dto', () => {
    const spy = jest
      .spyOn(MessagesService.prototype, 'createMessage')
      .mockReturnValue(of({ id: 'msg1' }) as never);
    const dto = { keyId: 'k1', localeId: 'l1', value: 'Hi' } as never;
    service.create(dto).subscribe({ error: () => undefined });
    expect(spy.mock.calls[0][0]).toEqual(dto);
  });

  it('update() calls MessagesService.updateMessage with the dto', () => {
    const spy = jest
      .spyOn(MessagesService.prototype, 'updateMessage')
      .mockReturnValue(of({ id: 'msg1' }) as never);
    const dto = { id: 'msg1', value: 'Hello' } as never;
    service.update(dto).subscribe({ error: () => undefined });
    expect(spy.mock.calls[0][0]).toEqual(dto);
  });

  it('delete() calls MessagesService.deleteMessage with the id', () => {
    const spy = jest
      .spyOn(MessagesService.prototype, 'deleteMessage')
      .mockReturnValue(of({ id: 'msg1' }) as never);
    service.delete('msg1').subscribe({ error: () => undefined });
    expect(spy.mock.calls[0][0]).toBe('msg1');
  });
});
