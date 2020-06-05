import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDividerModule } from '@angular/material/divider';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTabsModule } from '@angular/material/tabs';
import { CodemirrorModule } from '@ctrl/ngx-codemirror';
import { FilterFieldModule, NavbarModule } from '@dev/translatr-components';
import { HotkeysModule } from '@ngneat/hotkeys';
import { TranslocoModule } from '@ngneat/transloco';
import { EffectsModule } from '@ngrx/effects';
import { StoreModule } from '@ngrx/store';
import { AppFacade } from '../../../+state/app.facade';
import { SidenavModule } from '../../nav/sidenav/sidenav.module';
import { NavListModule } from '../../shared/nav-list/nav-list.module';
import { EditorEffects } from './+state/editor.effects';
import { EditorFacade } from './+state/editor.facade';
import { EDITOR_FEATURE_KEY, editorReducer, initialState as editorInitialState } from './+state/editor.reducer';
import { EditorPageRoutingModule } from './editor-page-routing.module';
import { EditorSelectorComponent } from './editor/editor-selector.component';
import { EditorComponent } from './editor/editor.component';
import { KeyEditorPageComponent } from './key-editor-page.component';
import { LocaleEditorPageComponent } from './locale-editor-page.component';

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
    NavbarModule.forRoot(AppFacade),
    FormsModule,
    NavListModule,
    FilterFieldModule,

    MatFormFieldModule,
    MatInputModule,
    MatMenuModule,
    MatDividerModule,
    MatButtonModule,
    MatListModule,
    MatTabsModule,
    MatIconModule,
    MatSnackBarModule,
    MatCardModule,

    CodemirrorModule,

    StoreModule.forFeature(EDITOR_FEATURE_KEY, editorReducer, {
      initialState: editorInitialState
    }),
    EffectsModule.forFeature([EditorEffects]),
    TranslocoModule,

    HotkeysModule
  ],
  providers: [EditorFacade]
})
export class EditorPageModule {}
