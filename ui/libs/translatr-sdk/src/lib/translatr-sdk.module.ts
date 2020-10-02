import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
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
  UserService
} from './services';

@NgModule({
  declarations: [],
  imports: [CommonModule, HttpClientModule],
  providers: [
    LanguageProvider,
    AccessTokenService,
    ActivityService,
    KeyService,
    LocaleService,
    MessageService,
    ProjectService,
    UserService,
    ErrorHandler
  ]
})
export class TranslatrSdkModule {}
