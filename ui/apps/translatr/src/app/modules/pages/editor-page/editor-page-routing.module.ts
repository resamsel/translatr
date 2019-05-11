import { NgModule } from "@angular/core";
import { RouterModule, Routes } from "@angular/router";
import { LocaleEditorPageComponent } from "./locale-editor-page.component";
import { KeyEditorPageComponent } from "./key-editor-page.component";

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
export class EditorPageRoutingModule {
}
