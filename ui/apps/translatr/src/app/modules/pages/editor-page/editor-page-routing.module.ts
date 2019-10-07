import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LocaleEditorPageComponent } from './locale-editor-page.component';
import { KeyEditorPageComponent } from './key-editor-page.component';
import { AuthGuard } from '../../../guards/auth.guard';

const routes: Routes = [
  {
    path: ':username/:projectName/locales/:localeName',
    component: LocaleEditorPageComponent,
    canActivate: [AuthGuard]
  },
  {
    path: ':username/:projectName/keys/:keyName',
    component: KeyEditorPageComponent,
    canActivate: [AuthGuard]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class EditorPageRoutingModule {
}
