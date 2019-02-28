import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {ProjectsPageModule} from "./modules/pages/projects-page/projects-page.module";
import {MainPageModule} from "./modules/pages/main-page/main-page.module";
import {ProjectPageModule} from "./modules/pages/project-page/project-page.module";
import {LayoutModule} from '@angular/cdk/layout';
import {MatButtonModule, MatToolbarModule} from '@angular/material';
import {SidenavModule} from "./modules/nav/sidenav/sidenav.module";

@NgModule({
  declarations: [
    AppComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    AppRoutingModule,
    SidenavModule,
    MainPageModule,
    ProjectsPageModule,
    ProjectPageModule,
    LayoutModule,
    MatToolbarModule,
    MatButtonModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule {
}
