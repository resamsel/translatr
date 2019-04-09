import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClientModule } from "@angular/common/http";
import { ActivityService } from "@dev/translatr-sdk/src/lib/services/activity.service";
import { AuthResolverService } from "@dev/translatr-sdk/src/lib/services/auth-resolver.service";
import { KeyService } from "@dev/translatr-sdk/src/lib/services/key.service";
import { LocaleService } from "@dev/translatr-sdk/src/lib/services/locale.service";
import { MessageService } from "@dev/translatr-sdk/src/lib/services/message.service";
import { ProjectService } from "@dev/translatr-sdk/src/lib/services/project.service";
import { UserService } from "@dev/translatr-sdk/src/lib/services/user.service";

@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    HttpClientModule
  ],
  providers: [
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
