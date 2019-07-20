import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProjectsPageComponent } from './projects-page.component';
import { ProjectsPageRoutingModule } from './projects-page-routing.module';
import { MatButtonModule, MatDialogModule, MatIconModule } from '@angular/material';
import { SidenavModule } from '../../nav/sidenav/sidenav.module';
import { ProjectListModule } from '../../shared/project-list/project-list.module';
import { StoreModule } from '@ngrx/store';
import { EffectsModule } from '@ngrx/effects';
import { initialState as projectsInitialState, PROJECTS_FEATURE_KEY, projectsReducer } from './+state/projects.reducer';
import { ProjectsEffects } from './+state/projects.effects';
import { ProjectsFacade } from './+state/projects.facade';
import { ProjectCardModule } from '../../shared/project-card/project-card.module';
import { GravatarModule } from 'ngx-gravatar';
import { ProjectEditDialogModule } from '../../shared/project-creation-dialog/project-edit-dialog.module';

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
    EffectsModule.forFeature([ProjectsEffects])
  ],
  providers: [ProjectsFacade]
})
export class ProjectsPageModule {
}
