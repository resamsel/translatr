import { CommonModule } from '@angular/common';
import { ModuleWithProviders, NgModule, Optional, SkipSelf, Type } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatOptionModule } from '@angular/material/core';
import { MatDividerModule } from '@angular/material/divider';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatMenuModule } from '@angular/material/menu';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterModule } from '@angular/router';
import { TranslocoModule } from '@ngneat/transloco';
import { GravatarModule } from 'ngx-gravatar';
import { AuthBarItemComponent } from './auth-bar-item/auth-bar-item.component';
import { AuthBarLanguageSwitcherComponent } from './auth-bar-language-switcher/auth-bar-language-switcher.component';
import { LanguageSwicher } from './language-swicher';
import { NavbarComponent } from './navbar.component';
import { SearchBarComponent } from './search-bar/search-bar.component';

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
      throw new Error('NavbarModule is already loaded. Import it in the page modules only.');
    }
  }

  static forRoot(languageSwitcher: Type<LanguageSwicher>): ModuleWithProviders<NavbarModule> {
    return {
      ngModule: NavbarModule,
      providers: [{ provide: LanguageSwicher, useClass: languageSwitcher }]
    };
  }
}
