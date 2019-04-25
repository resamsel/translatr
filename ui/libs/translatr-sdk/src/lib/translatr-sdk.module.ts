import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {HttpClientModule} from '@angular/common/http';
import {
  AccessTokenService,
  ActivityService,
  AuthResolverService,
  KeyService,
  LocaleService,
  MessageService,
  ProjectService,
  UserService
} from './services';

@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    HttpClientModule
  ],
  providers: [
    AccessTokenService,
    ActivityService,
    AuthResolverService,
    KeyService,
    LocaleService,
    MessageService,
    ProjectService,
    UserService
  ]
})
export class TranslatrSdkModule {
}
