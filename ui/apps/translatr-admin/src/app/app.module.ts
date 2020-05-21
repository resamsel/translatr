import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { AppComponent } from './app.component';
import { TranslatrSdkModule } from '@dev/translatr-sdk';
import { SidenavModule } from './modules/nav/sidenav/sidenav.module';
import { DashboardPageModule } from './modules/pages/dashboard-page/dashboard-page.module';
import { StoreModule } from '@ngrx/store';
import { EffectsModule } from '@ngrx/effects';
import { appReducer, initialState as appInitialState } from './+state/app.reducer';
import { AppEffects } from './+state/app.effects';
import { AppFacade } from './+state/app.facade';
import { NxModule } from '@nrwl/angular';
import { StoreDevtoolsModule } from '@ngrx/store-devtools';
import { environment } from '../environments/environment';
import { StoreRouterConnectingModule } from '@ngrx/router-store';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ENDPOINT_URL, LOGIN_URL, WINDOW } from '@translatr/utils';
import { LoginPageModule } from '@translatr/translatr-components/src/lib/modules/pages/login-page/login-page.module';
import { AppRoutingModule } from './app-routing.module';
import { FeatureFlagModule } from '@dev/translatr-components';
import { FeatureFlagFacade } from '@dev/translatr-model';
import { TranslocoRootModule } from './modules/transloco';

@NgModule({
  declarations: [AppComponent],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    TranslatrSdkModule,
    SidenavModule,
    AppRoutingModule,
    DashboardPageModule,
    LoginPageModule,
    FeatureFlagModule,
    NxModule.forRoot(),
    StoreModule.forRoot(
      {app: appReducer},
      {
        initialState: {app: appInitialState},
        metaReducers: [],
        runtimeChecks: {
          strictStateImmutability: true,
          strictActionImmutability: true
        }
      }
    ),
    EffectsModule.forRoot([AppEffects]),
    !environment.production ? StoreDevtoolsModule.instrument() : [],
    StoreRouterConnectingModule.forRoot(),
    TranslocoRootModule
  ],
  providers: [
    AppFacade,
    {provide: FeatureFlagFacade, useClass: AppFacade},
    {provide: WINDOW, useFactory: () => window},
    {provide: ENDPOINT_URL, useValue: environment.endpointUrl},
    {provide: LOGIN_URL, useValue: `${environment.endpointUrl}/login`}
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}
