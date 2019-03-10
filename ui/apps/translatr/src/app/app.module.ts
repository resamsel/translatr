import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { BrowserAnimationsModule } from "@angular/platform-browser/animations";
import { MainPageModule } from "./modules/pages/main-page/main-page.module";
import { ProjectPageModule } from "./modules/pages/project-page/project-page.module";
import { LayoutModule } from '@angular/cdk/layout';
import { MatButtonModule, MatToolbarModule } from '@angular/material';
import { SidenavModule } from "./modules/nav/sidenav/sidenav.module";
import { UserPageModule } from "./modules/pages/user-page/user-page.module";
import { HttpClientModule } from "@angular/common/http";
import { NxModule } from '@nrwl/nx';
import { ProjectsPageModule } from "./modules/pages/projects-page/projects-page.module";
import { UsersPageModule } from "./modules/pages/users-page/users-page.module";
import { StoreModule } from "@ngrx/store";
import { EffectsModule } from "@ngrx/effects";
import { StoreDevtoolsModule } from "@ngrx/store-devtools";

@NgModule({
  declarations: [AppComponent],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    AppRoutingModule,
    HttpClientModule,
    SidenavModule,
    MainPageModule,
    // DashboardPageModule,
    ProjectsPageModule,
    UsersPageModule,
    UserPageModule,
    ProjectPageModule,
    LayoutModule,
    MatToolbarModule,
    MatButtonModule,
    StoreModule.forRoot({}),
    StoreDevtoolsModule.instrument({}),
    EffectsModule.forRoot([]),
    NxModule.forRoot()
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule {
}
