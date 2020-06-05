import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { NgModule } from '@angular/core';
import { Router } from '@angular/router';
import { LOGIN_URL } from '@translatr/utils';
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
      useFactory: (notificationService: NotificationService, router: Router, loginUrl: string) =>
        new DefaultErrorHandler(notificationService, router, loginUrl),
      deps: [NotificationService, Router, LOGIN_URL]
    }
  ]
})
export class TranslatrSdkModule {}
