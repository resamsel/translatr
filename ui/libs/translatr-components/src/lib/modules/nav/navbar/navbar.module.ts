import { NgModule } from '@angular/core';
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

@NgModule({
  declarations: [NavbarComponent, AuthBarItemComponent, SearchBarComponent],
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
    MatOptionModule
  ],
  exports: [NavbarComponent, SearchBarComponent, AuthBarItemComponent]
})
export class NavbarModule {
}
