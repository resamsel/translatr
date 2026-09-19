import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LanguageSwicher } from '@dev/translatr-components';
import { EffectsModule } from '@ngrx/effects';
import { StoreModule } from '@ngrx/store';
import { AppFacade } from '../../../+state/app.facade';
import { AuthGuard } from '../../../guards/auth.guard';
import { ProjectStateModule } from '../../shared/project-state';
import { EditorEffects } from './+state/editor.effects';
import { EditorFacade } from './+state/editor.facade';
import {
  EDITOR_FEATURE_KEY,
  editorReducer,
  initialState as editorInitialState
} from './+state/editor.reducer';
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
  imports: [
    RouterModule.forChild(routes),
    ProjectStateModule,
    StoreModule.forFeature(EDITOR_FEATURE_KEY, editorReducer, {
      initialState: editorInitialState
    }),
    EffectsModule.forFeature([EditorEffects])
  ],
  exports: [RouterModule],
  providers: [EditorFacade, { provide: LanguageSwicher, useClass: AppFacade }]
})
export class EditorPageRoutingModule {}
