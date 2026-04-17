import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { RouterModule } from '@angular/router';
import { GravatarModule } from 'ngx-gravatar';
import { TimeAgoModule } from '../../pipes/time-ago/time-ago.pipe';
import { UserCardLinkComponent } from './user-card-link.component';
import { UserCardComponent } from './user-card.component';

@NgModule({
  declarations: [UserCardComponent, UserCardLinkComponent],
  imports: [CommonModule, RouterModule, MatCardModule, MatIconModule, GravatarModule, TimeAgoModule],
  exports: [UserCardComponent, UserCardLinkComponent]
})
export class UserCardModule {}
