import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { RouterModule } from '@angular/router';
import { GravatarModule } from 'ngx-gravatar';
import { MomentModule } from 'ngx-moment';
import { UserCardLinkComponent } from './user-card-link.component';
import { UserCardComponent } from './user-card.component';

@NgModule({
  declarations: [UserCardComponent, UserCardLinkComponent],
  imports: [CommonModule, RouterModule, MatCardModule, MatIconModule, GravatarModule, MomentModule],
  exports: [UserCardComponent, UserCardLinkComponent]
})
export class UserCardModule {}
