import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterModule } from '@angular/router';
import { GravatarModule } from 'ngx-gravatar';
import { TimeAgoModule } from '@dev/translatr-components';
import { ProjectCardLinkComponent } from './project-card-link.component';
import { ProjectCardComponent } from './project-card.component';

@NgModule({
  declarations: [ProjectCardComponent, ProjectCardLinkComponent],
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatIconModule,
    GravatarModule,
    TimeAgoModule,
    MatTooltipModule
  ],
  exports: [ProjectCardComponent, ProjectCardLinkComponent]
})
export class ProjectCardModule {}
