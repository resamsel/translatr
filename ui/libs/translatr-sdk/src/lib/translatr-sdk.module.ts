import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import {
  AccessTokenService,
  ActivityService,
  DefaultErrorHandler,
  ErrorHandler,
  KeyService,
  LocaleService,
  MessageService,
  ProjectService,
  UserService
} from './services';
import { Router } from '@angular/router';
import { LOGIN_URL } from '@translatr/utils';

@NgModule({
  declarations: [],
  imports: [CommonModule, HttpClientModule],
  providers: [
    AccessTokenService,
    ActivityService,
    KeyService,
    LocaleService,
    MessageService,
    ProjectService,
    UserService,
    {
      provide: ErrorHandler,
      useFactory: (router: Router, loginUrl: string) => new DefaultErrorHandler(router, loginUrl),
      deps: [Router, LOGIN_URL]
    }
  ]
})
export class TranslatrSdkModule {
}
