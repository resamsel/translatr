import {
  HttpClient,
  HttpEvent,
  HttpHandler,
  HttpRequest,
  HttpXhrBackend,
} from '@angular/common/http';
import { XhrFactory } from '@angular/common';
import { Injector, StaticProvider } from '@angular/core';
import {
  ACCESS_TOKEN,
  AccessTokenService,
  AccessTokensService,
  ActivityService,
  Configuration,
  ErrorHandler,
  KeyService,
  KeysService,
  LanguageProvider,
  LocaleService,
  LocalesService,
  MemberService,
  MembersService,
  MessageService,
  MessagesService,
  ProjectService,
  ProjectsService,
  UserService,
  UsersService,
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
    private readonly accessToken: string,
  ) {
    this.handler = new HttpXhrBackend(xhrFactory);
  }

  handle(req: HttpRequest<any>): Observable<HttpEvent<any>> {
    // Token precedence: an explicit `withAuth(token)` (carried on the request
    // context) wins, otherwise fall back to this injector's default token.
    const token = req.context.get(ACCESS_TOKEN) ?? this.accessToken;
    const params = req.params.has('access_token')
      ? req.params
      : req.params.set('access_token', token);
    // `clone` keeps headers/context/etc.; only the URL and params change.
    return this.handler.handle(req.clone({ url: `${this.baseUrl}${req.url}`, params }));
  }
}

const relativeConfig = () => new Configuration({ basePath: '' });

const providers: StaticProvider[] = [
  {
    provide: HttpClient,
    useFactory: (backend: HttpHandler): HttpClient => new HttpClient(backend),
    deps: [HttpHandler],
  },
  { provide: XhrFactory, useValue: new BrowserXhr() },
  { provide: ErrorHandler, useValue: new LoggingErrorHandler() },
  { provide: LanguageProvider, useValue: new LanguageProvider() },
  {
    provide: UserService,
    useFactory: (
      client: HttpClient,
      errorHandler: ErrorHandler,
      languageProvider: LanguageProvider,
    ) =>
      new UserService(
        client,
        errorHandler,
        languageProvider,
        new UsersService(client, '', relativeConfig()),
      ),
    deps: [HttpClient, ErrorHandler, LanguageProvider],
  },
  {
    provide: ProjectService,
    useFactory: (
      client: HttpClient,
      errorHandler: ErrorHandler,
      languageProvider: LanguageProvider,
    ) =>
      new ProjectService(
        client,
        errorHandler,
        languageProvider,
        new ProjectsService(client, '', relativeConfig()),
      ),
    deps: [HttpClient, ErrorHandler, LanguageProvider],
  },
  {
    provide: LocaleService,
    useFactory: (
      client: HttpClient,
      errorHandler: ErrorHandler,
      languageProvider: LanguageProvider,
    ) =>
      new LocaleService(
        client,
        errorHandler,
        languageProvider,
        new LocalesService(client, '', relativeConfig()),
      ),
    deps: [HttpClient, ErrorHandler, LanguageProvider],
  },
  {
    provide: KeyService,
    useFactory: (
      client: HttpClient,
      errorHandler: ErrorHandler,
      languageProvider: LanguageProvider,
    ) =>
      new KeyService(
        client,
        errorHandler,
        languageProvider,
        new KeysService(client, '', relativeConfig()),
      ),
    deps: [HttpClient, ErrorHandler, LanguageProvider],
  },
  {
    provide: MessageService,
    useFactory: (
      client: HttpClient,
      errorHandler: ErrorHandler,
      languageProvider: LanguageProvider,
    ) =>
      new MessageService(
        client,
        errorHandler,
        languageProvider,
        new MessagesService(client, '', relativeConfig()),
      ),
    deps: [HttpClient, ErrorHandler, LanguageProvider],
  },
  {
    provide: AccessTokenService,
    useFactory: (
      client: HttpClient,
      errorHandler: ErrorHandler,
      languageProvider: LanguageProvider,
    ) =>
      new AccessTokenService(
        client,
        errorHandler,
        languageProvider,
        new AccessTokensService(client, '', relativeConfig()),
      ),
    deps: [HttpClient, ErrorHandler, LanguageProvider],
  },
  {
    provide: ActivityService,
    useFactory: (
      client: HttpClient,
      errorHandler: ErrorHandler,
      languageProvider: LanguageProvider,
    ) => new ActivityService(client, errorHandler, languageProvider),
    deps: [HttpClient, ErrorHandler, LanguageProvider],
  },
  {
    provide: MemberService,
    useFactory: (
      client: HttpClient,
      errorHandler: ErrorHandler,
      languageProvider: LanguageProvider,
    ) =>
      new MemberService(
        client,
        errorHandler,
        languageProvider,
        new MembersService(client, '', relativeConfig()),
      ),
    deps: [HttpClient, ErrorHandler, LanguageProvider],
  },
];

export const createInjector = (baseUrl: string, accessToken: string): Injector => {
  return Injector.create({
    providers: [
      ...providers,
      {
        provide: HttpHandler,
        useFactory: (xhrFactory: XhrFactory) => new MyHttpHandler(xhrFactory, baseUrl, accessToken),
        deps: [XhrFactory],
      },
    ],
  });
};
