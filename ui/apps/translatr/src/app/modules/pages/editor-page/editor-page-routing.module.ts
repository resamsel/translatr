import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from '../../../guards/auth.guard';
import { KeyEditorPageComponent } from './key-editor-page.component';
import { LocaleEditorPageComponent } from './locale-editor-page.component';

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
export class EditorPageRoutingModule {}
