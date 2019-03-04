import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ProjectPageComponent} from './project-page.component';
import {ProjectPageRoutingModule} from "./project-page-routing.module";
import {
  MatButtonModule,
  MatCardModule,
  MatChipsModule,
  MatIconModule, MatListModule,
  MatTabsModule,
  MatToolbarModule
} from "@angular/material";
import {MomentModule} from "ngx-moment";
import {SidenavModule} from "../../nav/sidenav/sidenav.module";
import {ProjectInfoComponent} from './project-info/project-info.component';
import {ProjectKeysComponent} from './project-keys/project-keys.component';
import { KeyListComponent } from './project-keys/key-list/key-list.component';
import { ProjectLocalesComponent } from './project-locales/project-locales.component';
import { LocaleListComponent } from './project-locales/locale-list/locale-list.component';
import { ProjectMembersComponent } from './project-members/project-members.component';
import { MemberListComponent } from './project-members/member-list/member-list.component';
import {GravatarModule} from "ngx-gravatar";

@NgModule({
  declarations: [ProjectPageComponent, ProjectInfoComponent, ProjectKeysComponent, KeyListComponent, ProjectLocalesComponent, LocaleListComponent, ProjectMembersComponent, MemberListComponent],
  imports: [
    ProjectPageRoutingModule,
    CommonModule,
    SidenavModule,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatChipsModule,
    MatTabsModule,
    MatListModule,
    MomentModule,
    GravatarModule
  ]
})
export class ProjectPageModule {
}
