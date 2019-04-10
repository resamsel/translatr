import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';

import {DashboardPageRoutingModule} from './dashboard-page-routing.module';
import {DashboardPageComponent} from './dashboard-page.component';
import {MatButtonModule, MatIconModule, MatTableModule, MatTabsModule} from "@angular/material";
import {SidenavModule} from "../../nav/sidenav/sidenav.module";
import {DashboardUsersComponent} from './dashboard-users/dashboard-users.component';
import {DashboardInfoComponent} from './dashboard-info/dashboard-info.component';
import {MomentModule} from "ngx-moment";

@NgModule({
  declarations: [DashboardPageComponent, DashboardUsersComponent, DashboardInfoComponent],
  imports: [
    CommonModule,
    DashboardPageRoutingModule,
    SidenavModule,
    MatIconModule,
    MatButtonModule,
    MatTabsModule,
    MatTableModule,
    MomentModule
  ]
})
export class DashboardPageModule {
}
