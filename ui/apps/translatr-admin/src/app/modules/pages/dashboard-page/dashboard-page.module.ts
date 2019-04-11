import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';

import {DashboardPageRoutingModule} from './dashboard-page-routing.module';
import {DashboardPageComponent} from './dashboard-page.component';
import {MatButtonModule, MatDialogModule, MatIconModule, MatTableModule, MatTabsModule} from "@angular/material";
import {SidenavModule} from "../../nav/sidenav/sidenav.module";
import {DashboardUsersComponent} from './dashboard-users/dashboard-users.component';
import {DashboardInfoComponent} from './dashboard-info/dashboard-info.component';
import {MomentModule} from "ngx-moment";
import {UserEditDialogModule} from "@dev/translatr-components/src/lib/modules/user/user-edit-dialog/user-edit-dialog.module";
import { DashboardProjectsComponent } from './dashboard-projects/dashboard-projects.component';

@NgModule({
  declarations: [DashboardPageComponent, DashboardUsersComponent, DashboardInfoComponent, DashboardProjectsComponent],
  imports: [
    CommonModule,
    DashboardPageRoutingModule,
    SidenavModule,
    UserEditDialogModule,
    MatIconModule,
    MatButtonModule,
    MatTabsModule,
    MatTableModule,
    MatDialogModule,
    MomentModule
  ]
})
export class DashboardPageModule {
}
