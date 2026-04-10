import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import {
  AccessTokenService,
  ActivityService,
  ErrorHandler,
  KeyService,
  LanguageProvider,
  LocaleService,
  MessageService,
  ProjectService,
  StatisticService,
  UserService
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
    ErrorHandler
  ]
})
export class TranslatrSdkModule {}
