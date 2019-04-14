import { HttpClient, HttpEvent, HttpHandler, HttpRequest, HttpXhrBackend, XhrFactory } from "@angular/common/http";
import { Observable } from "rxjs";
import { Injector, StaticProvider } from "@angular/core";
import { ProjectService } from "../../libs/translatr-sdk/src/lib/services/project.service";
import { UserService } from "../../libs/translatr-sdk/src/lib/services/user.service";
import { XMLHttpRequest } from 'xmlhttprequest';

export class BrowserXhr implements XhrFactory {
  constructor() {
  }

  build(): any {
    return <any>(new XMLHttpRequest());
  }
}

class MyHttpHandler implements HttpHandler {
  private handler: HttpXhrBackend;

  constructor(
    private readonly xhrFactory: XhrFactory,
    private readonly baseUrl: string,
    private readonly accessToken: string
  ) {
    this.handler = new HttpXhrBackend(xhrFactory);
  }

  handle(req: HttpRequest<any>): Observable<HttpEvent<any>> {
    const url = `${this.baseUrl}${req.url}`;
    const newReq = new HttpRequest(req.method, url, req.body, {
      headers: req.headers.append('x-access-token', this.accessToken),
      reportProgress: req.reportProgress,
      params: req.params.append('access_token', this.accessToken),
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
  {provide: XhrFactory, useValue: new BrowserXhr()},
  {
    provide: UserService,
    useFactory: (client: HttpClient) => new UserService(client),
    deps: [HttpClient]
  },
  {
    provide: ProjectService,
    useFactory: (client: HttpClient) => new ProjectService(client),
    deps: [HttpClient]
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
