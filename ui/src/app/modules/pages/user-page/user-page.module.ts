import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { UserPageRoutingModule } from './user-page-routing.module';
import { UserPageComponent } from './user-page.component';
import { UserInfoComponent } from './user-info/user-info.component';
import {MatChipsModule, MatIconModule, MatTabsModule} from "@angular/material";
import {RouterModule} from "@angular/router";
import {SidenavModule} from "../../nav/sidenav/sidenav.module";
import {MomentModule} from "ngx-moment";

@NgModule({
  declarations: [UserPageComponent, UserInfoComponent],
  imports: [
    CommonModule,
    RouterModule,
    UserPageRoutingModule,
    SidenavModule,
    MatIconModule,
    MatTabsModule,
    MatChipsModule,
    MomentModule
  ]
})
export class UserPageModule { }
