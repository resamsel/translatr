import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EditorPageRoutingModule } from './editor-page-routing.module';
import { LocaleEditorPageComponent } from './locale-editor-page.component';
import { SidenavModule } from '../../nav/sidenav/sidenav.module';
import {
  MatButtonModule,
  MatDividerModule,
  MatFormFieldModule,
  MatIconModule,
  MatInputModule,
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
import { FormsModule } from '@angular/forms';
import { NavListModule } from '../../shared/nav-list/nav-list.module';
import { EditorComponent } from './editor/editor.component';
import { EditorSelectorComponent } from './editor/editor-selector.component';
import { KeyEditorPageComponent } from './key-editor-page.component';
import { NavbarModule } from '@dev/translatr-components';

@NgModule({
  declarations: [
    LocaleEditorPageComponent,
    EditorComponent,
    EditorSelectorComponent,
    KeyEditorPageComponent
  ],
  imports: [
    CommonModule,
    EditorPageRoutingModule,
    SidenavModule,
    NavbarModule,
    FormsModule,
    NavListModule,
    MatFormFieldModule,
    MatInputModule,
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
export class EditorPageModule {}
