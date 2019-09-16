import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MomentModule } from 'ngx-moment';
import { GravatarModule } from 'ngx-gravatar';
import { ActivityListComponent } from './activity-list.component';
import { RouterModule } from '@angular/router';
import { ActivityContentModule } from '../activity-content/activity-content.module';
import { TagModule } from '@translatr/translatr-components/src/lib/modules/tag/tag.module';
import { NavListModule } from '../nav-list/nav-list.module';

@NgModule({
  declarations: [ActivityListComponent],
  imports: [
    CommonModule,
    RouterModule,

    MatListModule,
    MatIconModule,
    MatChipsModule,

    MomentModule,
    GravatarModule,

    ActivityContentModule,
    TagModule,
    NavListModule
  ],
  exports: [ActivityListComponent]
})
export class ActivityListModule {}
