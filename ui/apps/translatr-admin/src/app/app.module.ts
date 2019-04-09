import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { AppComponent } from './app.component';
import { RouterModule } from '@angular/router';
import { TranslatrSdkModule } from "@dev/translatr-sdk";
import { SidenavModule } from "./modules/nav/sidenav/sidenav.module";
import { DashboardPageModule } from "./modules/pages/dashboard-page/dashboard-page.module";

@NgModule({
  declarations: [AppComponent],
  imports: [
    BrowserModule,
    TranslatrSdkModule,
    SidenavModule,
    RouterModule.forRoot([], {initialNavigation: 'enabled'}),
    DashboardPageModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule {
}
