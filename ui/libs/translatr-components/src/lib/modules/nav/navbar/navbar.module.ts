import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {NavbarComponent} from './navbar.component';
import {
  MatButtonModule,
  MatDividerModule,
  MatFormFieldModule,
  MatIconModule,
  MatInputModule,
  MatMenuModule,
  MatToolbarModule
} from '@angular/material';
import {AuthBarItemComponent} from './auth-bar-item/auth-bar-item.component';
import {RouterModule} from '@angular/router';
import {GravatarModule} from 'ngx-gravatar';
import {SearchBarComponent} from './search-bar/search-bar.component';
import {ReactiveFormsModule} from '@angular/forms';

@NgModule({
  declarations: [
    NavbarComponent,
    AuthBarItemComponent,
    SearchBarComponent
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
    GravatarModule
  ],
  exports: [
    NavbarComponent,
    SearchBarComponent,
    AuthBarItemComponent
  ]
})
export class NavbarModule {
}
