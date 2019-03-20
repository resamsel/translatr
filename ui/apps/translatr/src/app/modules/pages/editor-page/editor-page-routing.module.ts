import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { LocaleEditorPageComponent } from "./locale-editor-page.component";

const routes: Routes = [
  {
    path: ':username/:projectName/locales/:localeName',
    component: LocaleEditorPageComponent,
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class EditorPageRoutingModule { }
