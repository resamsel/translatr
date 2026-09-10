import { CommonModule } from '@angular/common';
import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { NgModule } from '@angular/core';
import { BASE_PATH } from './generated/variables';
import {
  AccessTokenInterceptor,
  AccessTokenService,
  AcceptLanguageInterceptor,
  ActivityService,
  ErrorHandler,
  KeyService,
  LanguageProvider,
  LocaleService,
  MessageService,
  ProjectService,
  StatisticService,
  UserService,
} from './services';

@NgModule({
  declarations: [],
  imports: [CommonModule],
  providers: [
    LanguageProvider,
    AccessTokenService,
    ActivityService,
    KeyService,
    LocaleService,
    MessageService,
    ProjectService,
    UserService,
    StatisticService,
    ErrorHandler,
    // Generated API clients fall back to an absolute `http://localhost` base
    // path unless a BASE_PATH is provided; '' keeps their requests relative to
    // the app's own origin, matching the hand-written services.
    { provide: BASE_PATH, useValue: '' },
    // Sets Accept-Language on every same-origin API request (generated clients
    // included), replacing the per-call header the services used to add.
    { provide: HTTP_INTERCEPTORS, useClass: AcceptLanguageInterceptor, multi: true },
    // Turns an `AbstractService.withAuth(token)` context into an `?access_token=`
    // query param, replacing the removed create/update/delete `options` param.
    { provide: HTTP_INTERCEPTORS, useClass: AccessTokenInterceptor, multi: true },
  ],
})
export class TranslatrSdkModule {}
