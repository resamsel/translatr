import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProjectsPageComponent } from './projects-page.component';
import { ProjectsPageRoutingModule } from './projects-page-routing.module';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { SidenavModule } from '../../nav/sidenav/sidenav.module';
import { ProjectListModule } from '../../shared/project-list/project-list.module';
import { StoreModule } from '@ngrx/store';
import { EffectsModule } from '@ngrx/effects';
import { initialState as projectsInitialState, PROJECTS_FEATURE_KEY, projectsReducer } from './+state/projects.reducer';
import { ProjectsEffects } from './+state/projects.effects';
import { ProjectsFacade } from './+state/projects.facade';
import { ProjectCardModule } from '../../shared/project-card/project-card.module';
import { GravatarModule } from 'ngx-gravatar';
import { ProjectEditDialogModule } from '../../shared/project-edit-dialog/project-edit-dialog.module';
import { MatTooltipModule } from '@angular/material';
import { ProjectCardListModule } from '../../shared/project-card-list/project-card-list.module';
import { FeatureFlagModule } from '@dev/translatr-components';
import { TranslocoModule } from '@ngneat/transloco';

@NgModule({
  declarations: [ProjectsPageComponent],
  imports: [
    CommonModule,
    ProjectsPageRoutingModule,
    SidenavModule,
    ProjectListModule,
    ProjectCardModule,
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
    FeatureFlagModule,
    TranslocoModule
  ],
  providers: [ProjectsFacade]
})
export class ProjectsPageModule {
}
