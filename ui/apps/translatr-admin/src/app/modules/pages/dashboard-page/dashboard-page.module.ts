import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardPageRoutingModule } from './dashboard-page-routing.module';
import { DashboardPageComponent } from './dashboard-page.component';
import {
  MatButtonModule,
  MatCheckboxModule,
  MatDialogModule,
  MatIconModule,
  MatInputModule,
  MatMenuModule,
  MatPaginatorModule,
  MatSelectModule,
  MatSnackBarModule,
  MatTableModule,
  MatTabsModule
} from "@angular/material";
import { SidenavModule } from "../../nav/sidenav/sidenav.module";
import { DashboardUsersComponent } from './dashboard-users/dashboard-users.component';
import { DashboardInfoComponent } from './dashboard-info/dashboard-info.component';
import { MomentModule } from "ngx-moment";
import { UserEditDialogModule } from "@dev/translatr-components/src/lib/modules/user/user-edit-dialog/user-edit-dialog.module";
import { DashboardProjectsComponent } from './dashboard-projects/dashboard-projects.component';
import { ReactiveFormsModule } from "@angular/forms";
import { DashboardAccessTokensComponent } from './dashboard-access-tokens/dashboard-access-tokens.component';
import { GravatarModule } from "ngx-gravatar";
import { ButtonModule } from "@dev/translatr-components/src/lib/modules/button/button.module";
import { DashboardUserComponent } from './dashboard-user/dashboard-user.component';
import { UserCardModule } from "@dev/translatr-components/src/lib/modules/user/user-card/user-card.module";
import { EntityTableModule } from "@dev/translatr-components/src/lib/modules/entity/entity-table/entity-table.module";

@NgModule({
  declarations: [DashboardPageComponent, DashboardUsersComponent, DashboardInfoComponent, DashboardProjectsComponent, DashboardAccessTokensComponent, DashboardUserComponent],
  imports: [
    CommonModule,
    DashboardPageRoutingModule,
    SidenavModule,
    UserEditDialogModule,
    ButtonModule,
    UserCardModule,
    ReactiveFormsModule,
    MatIconModule,
    MatButtonModule,
    MatTabsModule,
    MatTableModule,
    MatDialogModule,
    MomentModule,
    MatMenuModule,
    MatSelectModule,
    MatInputModule,
    MatPaginatorModule,
    MatCheckboxModule,
    MatSnackBarModule,
    GravatarModule,
    EntityTableModule
  ]
})
export class DashboardPageModule {
}
