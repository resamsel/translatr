/* tslint:disable:max-classes-per-file */
import {
  HttpClient,
  HttpEvent,
  HttpHandler,
  HttpRequest,
  HttpXhrBackend,
  XhrFactory
} from '@angular/common/http';
import { Injector, StaticProvider } from '@angular/core';
import {
  AccessTokenService,
  ErrorHandler,
  KeyService,
  LocaleService,
  MessageService,
  ProjectService,
  UserService
} from '@dev/translatr-sdk';
import { Observable } from 'rxjs';
import { XMLHttpRequest } from 'xmlhttprequest';

export class BrowserXhr implements XhrFactory {
  constructor() {}

  build(): any {
    return new XMLHttpRequest() as any;
  }
}

class MyHttpHandler implements HttpHandler {
  private handler: HttpXhrBackend;

  constructor(
    readonly xhrFactory: XhrFactory,
    private readonly baseUrl: string,
    private readonly accessToken: string
  ) {
    this.handler = new HttpXhrBackend(xhrFactory);
  }

  handle(req: HttpRequest<any>): Observable<HttpEvent<any>> {
    const url = `${this.baseUrl}${req.url}`;
    const params = !req.params.has('access_token')
      ? req.params.set('access_token', this.accessToken)
      : req.params;
    const newReq = new HttpRequest(req.method, url, req.body, {
      reportProgress: req.reportProgress,
      params,
      responseType: req.responseType,
      withCredentials: req.withCredentials
    });
    return this.handler.handle(newReq);
  }
}

const providers: StaticProvider[] = [
  {
    provide: HttpClient,
    useFactory: (backend: HttpHandler): HttpClient => new HttpClient(backend),
    deps: [HttpHandler]
  },
  { provide: XhrFactory, useValue: new BrowserXhr() },
  { provide: ErrorHandler, useValue: new ErrorHandler() },
  {
    provide: UserService,
    useFactory: (client: HttpClient, errorHandler: ErrorHandler) =>
      new UserService(client, errorHandler),
    deps: [HttpClient, ErrorHandler]
  },
  {
    provide: ProjectService,
    useFactory: (client: HttpClient, errorHandler: ErrorHandler) =>
      new ProjectService(client, errorHandler),
    deps: [HttpClient, ErrorHandler]
  },
  {
    provide: LocaleService,
    useFactory: (client: HttpClient, errorHandler: ErrorHandler) =>
      new LocaleService(client, errorHandler),
    deps: [HttpClient, ErrorHandler]
  },
  {
    provide: KeyService,
    useFactory: (client: HttpClient, errorHandler: ErrorHandler) =>
      new KeyService(client, errorHandler),
    deps: [HttpClient, ErrorHandler]
  },
  {
    provide: MessageService,
    useFactory: (client: HttpClient, errorHandler: ErrorHandler) =>
      new MessageService(client, errorHandler),
    deps: [HttpClient, ErrorHandler]
  },
  {
    provide: AccessTokenService,
    useFactory: (client: HttpClient, errorHandler: ErrorHandler) =>
      new AccessTokenService(client, errorHandler),
    deps: [HttpClient, ErrorHandler]
  }
];

export const createInjector = (baseUrl: string, accessToken: string): Injector => {
  return Injector.create({
    providers: [
      ...providers,
      {
        provide: HttpHandler,
        useFactory: (xhrFactory: XhrFactory) => new MyHttpHandler(xhrFactory, baseUrl, accessToken),
        deps: [XhrFactory]
      }
    ]
  });
};
