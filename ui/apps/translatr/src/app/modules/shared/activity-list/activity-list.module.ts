import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MomentModule } from 'ngx-moment';
import { GravatarModule } from 'ngx-gravatar';
import { ActivityListComponent } from './activity-list.component';
import { RouterModule } from '@angular/router';
import { TagModule } from '@translatr/translatr-components/src/lib/modules/tag/tag.module';
import { NavListModule } from '../nav-list/nav-list.module';
import { MatTooltipModule } from '@angular/material';
import { ActivityProjectLinkComponent } from './activity-project-link/activity-project-link.component';
import { ActivityLocaleLinkComponent } from './activity-locale-link/activity-locale-link.component';
import { ActivityKeyLinkComponent } from './activity-key-link/activity-key-link.component';
import { ActivityMemberLinkComponent } from './activity-member-link/activity-member-link.component';
import { ActivityMessageLinkComponent } from './activity-message-link/activity-message-link.component';
import { ActivityAccessTokenLinkComponent } from './activity-access-token-link/activity-access-token-link.component';
import { TranslocoModule } from '@ngneat/transloco';

@NgModule({
  declarations: [
    ActivityListComponent,
    ActivityProjectLinkComponent,
    ActivityLocaleLinkComponent,
    ActivityKeyLinkComponent,
    ActivityMemberLinkComponent,
    ActivityMessageLinkComponent,
    ActivityAccessTokenLinkComponent
  ],
  imports: [
    CommonModule,
    RouterModule,

    TagModule,
    NavListModule,

    MatListModule,
    MatIconModule,
    MatChipsModule,
    MatTooltipModule,

    MomentModule,
    GravatarModule,
    TranslocoModule
  ],
  exports: [ActivityListComponent]
})
export class ActivityListModule {
}
