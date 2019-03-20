import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EditorPageRoutingModule } from './editor-page-routing.module';
import { LocaleEditorPageComponent } from './locale-editor-page.component';
import { SidenavModule } from '../../nav/sidenav/sidenav.module';
import {
  MatButtonModule,
  MatDividerModule,
  MatIconModule,
  MatListModule,
  MatMenuModule,
  MatSnackBarModule,
  MatTabsModule
} from '@angular/material';
import { StoreModule } from '@ngrx/store';
import { EffectsModule } from '@ngrx/effects';
import { EDITOR_FEATURE_KEY, editorReducer, initialState as editorInitialState } from './+state/editor.reducer';
import { EditorEffects } from './+state/editor.effects';
import { EditorFacade } from './+state/editor.facade';
import { CodemirrorModule } from '@ctrl/ngx-codemirror';
import { FormsModule } from "@angular/forms";
import { NavListModule } from "../../shared/nav-list/nav-list.module";

@NgModule({
  declarations: [LocaleEditorPageComponent],
  imports: [
    CommonModule,
    EditorPageRoutingModule,
    SidenavModule,
    FormsModule,
    NavListModule,
    MatMenuModule,
    MatDividerModule,
    MatButtonModule,
    MatListModule,
    MatTabsModule,
    MatIconModule,
    MatSnackBarModule,
    CodemirrorModule,
    StoreModule.forFeature(EDITOR_FEATURE_KEY, editorReducer, {
      initialState: editorInitialState
    }),
    EffectsModule.forFeature([EditorEffects])
  ],
  providers: [EditorFacade]
})
export class EditorPageModule {
}
