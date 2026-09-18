import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { FeatureFlagDirective, FeatureFlagClassDirective } from '@dev/translatr-components';
import { TranslocoModule } from '@jsverse/transloco';
import { EffectsModule } from '@ngrx/effects';
import { StoreModule } from '@ngrx/store';
import { GravatarModule } from 'ngx-gravatar';
import { SidenavModule } from '../../nav/sidenav/sidenav.module';
import { ProjectCardListModule } from '../../shared/project-card-list/project-card-list.module';
import { ProjectCardComponent } from '../../shared/project-card/project-card.component';
import { ProjectCardLinkComponent } from '../../shared/project-card/project-card-link.component';
import { ProjectEditDialogModule } from '../../shared/project-edit-dialog/project-edit-dialog.module';
import { ProjectListModule } from '../../shared/project-list/project-list.module';
import { ProjectsEffects } from './+state/projects.effects';
import { ProjectsFacade } from './+state/projects.facade';
import {
  initialState as projectsInitialState,
  PROJECTS_FEATURE_KEY,
  projectsReducer
} from './+state/projects.reducer';
import { ProjectsPageRoutingModule } from './projects-page-routing.module';
import { ProjectsPageComponent } from './projects-page.component';

@NgModule({
  declarations: [ProjectsPageComponent],
  imports: [
    CommonModule,
    ProjectsPageRoutingModule,
    SidenavModule,
    ProjectListModule,
    ProjectCardComponent, ProjectCardLinkComponent,
    ProjectEditDialogModule,

    MatIconModule,
    MatDialogModule,
    MatButtonModule,

    GravatarModule,
    StoreModule.forFeature(PROJECTS_FEATURE_KEY, projectsReducer, {
      initialState: projectsInitialState
    }),
    EffectsModule.forFeature([ProjectsEffects]),
    MatTooltipModule,
    ProjectCardListModule,
    FeatureFlagDirective, FeatureFlagClassDirective,
    TranslocoModule
  ],
  providers: [ProjectsFacade]
})
export class ProjectsPageModule {}
