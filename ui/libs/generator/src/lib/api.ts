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
  ActivityService,
  ErrorHandler,
  KeyService,
  LanguageProvider,
  LocaleService,
  MessageService,
  ProjectService,
  UserService
} from '@dev/translatr-sdk';
import { Observable } from 'rxjs';
import { XMLHttpRequest } from 'xmlhttprequest';
import { LoggingErrorHandler } from './logging-error-handler';

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
  { provide: ErrorHandler, useValue: new LoggingErrorHandler() },
  { provide: LanguageProvider, useValue: new LanguageProvider() },
  {
    provide: UserService,
    useFactory: (
      client: HttpClient,
      errorHandler: ErrorHandler,
      languageProvider: LanguageProvider
    ) => new UserService(client, errorHandler, languageProvider),
    deps: [HttpClient, ErrorHandler, LanguageProvider]
  },
  {
    provide: ProjectService,
    useFactory: (
      client: HttpClient,
      errorHandler: ErrorHandler,
      languageProvider: LanguageProvider
    ) => new ProjectService(client, errorHandler, languageProvider),
    deps: [HttpClient, ErrorHandler, LanguageProvider]
  },
  {
    provide: LocaleService,
    useFactory: (
      client: HttpClient,
      errorHandler: ErrorHandler,
      languageProvider: LanguageProvider
    ) => new LocaleService(client, errorHandler, languageProvider),
    deps: [HttpClient, ErrorHandler, LanguageProvider]
  },
  {
    provide: KeyService,
    useFactory: (
      client: HttpClient,
      errorHandler: ErrorHandler,
      languageProvider: LanguageProvider
    ) => new KeyService(client, errorHandler, languageProvider),
    deps: [HttpClient, ErrorHandler, LanguageProvider]
  },
  {
    provide: MessageService,
    useFactory: (
      client: HttpClient,
      errorHandler: ErrorHandler,
      languageProvider: LanguageProvider
    ) => new MessageService(client, errorHandler, languageProvider),
    deps: [HttpClient, ErrorHandler, LanguageProvider]
  },
  {
    provide: AccessTokenService,
    useFactory: (
      client: HttpClient,
      errorHandler: ErrorHandler,
      languageProvider: LanguageProvider
    ) => new AccessTokenService(client, errorHandler, languageProvider),
    deps: [HttpClient, ErrorHandler, LanguageProvider]
  },
  {
    provide: ActivityService,
    useFactory: (
      client: HttpClient,
      errorHandler: ErrorHandler,
      languageProvider: LanguageProvider
    ) => new ActivityService(client, errorHandler, languageProvider),
    deps: [HttpClient, ErrorHandler, LanguageProvider]
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
