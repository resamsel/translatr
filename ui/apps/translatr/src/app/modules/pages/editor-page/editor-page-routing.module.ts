import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { LocaleEditorPageComponent } from "./locale-editor-page.component";
import {KeyEditorPageComponent} from "./key-editor-page.component";
import {AuthResolverService} from "../../../../../../../libs/translatr-sdk/src/lib/services/auth-resolver.service";

const routes: Routes = [
  {
    path: ':username/:projectName/locales/:localeName',
    component: LocaleEditorPageComponent
  },
  {
    path: ':username/:projectName/keys/:keyName',
    component: KeyEditorPageComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class EditorPageRoutingModule { }
