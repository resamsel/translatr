import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProjectCardComponent } from './project-card.component';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { GravatarModule } from 'ngx-gravatar';
import { MomentModule } from 'ngx-moment';
import { ProjectCardLinkComponent } from './project-card-link.component';
import { MatTooltipModule } from '@angular/material/tooltip';

@NgModule({
  declarations: [ProjectCardComponent, ProjectCardLinkComponent],
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatIconModule,
    GravatarModule,
    MomentModule,
    MatTooltipModule
  ],
  exports: [ProjectCardComponent, ProjectCardLinkComponent]
})
export class ProjectCardModule {}
