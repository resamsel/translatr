import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatChipsModule, MatIconModule, MatListModule } from '@angular/material';
import { MomentModule } from 'ngx-moment';
import { GravatarModule } from 'ngx-gravatar';
import { ActivityListComponent } from './activity-list.component';
import { RouterModule } from '@angular/router';
import { ActivityContentModule } from '../activity-content/activity-content.module';
import { TagModule } from '@translatr/translatr-components/src/lib/modules/tag/tag.module';

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
    TagModule
  ],
  exports: [ActivityListComponent]
})
export class ActivityListModule {}
