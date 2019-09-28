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
  NotificationService,
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
      useFactory: (
        router: Router,
        notificationService: NotificationService,
        loginUrl: string
      ) => new DefaultErrorHandler(router, notificationService, loginUrl),
      deps: [Router, NotificationService, LOGIN_URL]
    }
  ]
})
export class TranslatrSdkModule {
}
