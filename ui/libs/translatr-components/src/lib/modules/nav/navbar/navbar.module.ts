import { ModuleWithProviders, NgModule, Optional, SkipSelf, Type } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavbarComponent } from './navbar.component';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatMenuModule } from '@angular/material/menu';
import { MatToolbarModule } from '@angular/material/toolbar';
import { AuthBarItemComponent } from './auth-bar-item/auth-bar-item.component';
import { RouterModule } from '@angular/router';
import { GravatarModule } from 'ngx-gravatar';
import { SearchBarComponent } from './search-bar/search-bar.component';
import { ReactiveFormsModule } from '@angular/forms';
import { MatAutocompleteModule, MatChipsModule, MatOptionModule, MatTooltipModule } from '@angular/material';
import { AuthBarLanguageSwitcherComponent } from './auth-bar-language-switcher/auth-bar-language-switcher.component';
import { TranslocoModule } from '@ngneat/transloco';
import { LanguageSwicher } from './language-swicher';

@NgModule({
  declarations: [
    NavbarComponent,
    AuthBarItemComponent,
    SearchBarComponent,
    AuthBarLanguageSwitcherComponent
  ],
  imports: [
    CommonModule,
    MatIconModule,
    MatToolbarModule,
    MatButtonModule,
    MatMenuModule,
    MatDividerModule,
    MatInputModule,
    MatFormFieldModule,
    ReactiveFormsModule,
    RouterModule,
    GravatarModule,
    MatTooltipModule,
    MatChipsModule,
    MatAutocompleteModule,
    MatOptionModule,
    TranslocoModule
  ],
  exports: [
    NavbarComponent,
    SearchBarComponent,
    AuthBarItemComponent,
    AuthBarLanguageSwitcherComponent
  ]
})
export class NavbarModule {
  constructor(@Optional() @SkipSelf() parentModule?: NavbarModule) {
    if (parentModule) {
      throw new Error(
        'NavbarModule is already loaded. Import it in the page modules only.');
    }
  }

  static forRoot(languageSwitcher: Type<LanguageSwicher>): ModuleWithProviders<NavbarModule> {
    return {
      ngModule: NavbarModule,
      providers: [
        {provide: LanguageSwicher, useClass: languageSwitcher}
      ]
    };
  }
}
