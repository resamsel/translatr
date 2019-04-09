import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { LocaleEditorPageComponent } from "./locale-editor-page.component";
import {KeyEditorPageComponent} from "./key-editor-page.component";
import {AuthResolverService} from "../../../../../../../libs/translatr-sdk/src/lib/services/auth-resolver.service";

const routes: Routes = [
  {
    path: ':username/:projectName/locales/:localeName',
    component: LocaleEditorPageComponent,
    resolve: {
      me: AuthResolverService
    }
  },
  {
    path: ':username/:projectName/keys/:keyName',
    component: KeyEditorPageComponent,
    resolve: {
      me: AuthResolverService
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class EditorPageRoutingModule { }
