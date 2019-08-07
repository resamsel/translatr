import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { LayoutModule } from '@angular/cdk/layout';
import { MatButtonModule } from '@angular/material/button';
import { MatToolbarModule } from '@angular/material/toolbar';
import { SidenavModule } from './modules/nav/sidenav/sidenav.module';
import { NxModule } from '@nrwl/nx';
import { StoreModule } from '@ngrx/store';
import { EffectsModule } from '@ngrx/effects';
import { StoreDevtoolsModule } from '@ngrx/store-devtools';
import { TranslatrSdkModule } from '@dev/translatr-sdk';
import { appReducer, initialState as appInitialState } from './+state/app.reducer';
import { AppEffects } from './+state/app.effects';
import { AppFacade } from './+state/app.facade';
import { environment } from '../environments/environment';
import { storeFreeze } from 'ngrx-store-freeze';
import { ENDPOINT_URL, LOGIN_URL } from '@translatr/utils';

@NgModule({
  declarations: [AppComponent],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,

    AppRoutingModule,
    TranslatrSdkModule,
    SidenavModule,
    LayoutModule,

    MatToolbarModule,
    MatButtonModule,

    NxModule.forRoot(),
    StoreModule.forRoot(
      { app: appReducer },
      {
        initialState: { app: appInitialState },
        metaReducers: !environment.production ? [storeFreeze] : []
      }
    ),
    EffectsModule.forRoot([AppEffects]),
    !environment.production ? StoreDevtoolsModule.instrument() : []
  ],
  providers: [
    AppFacade,
    { provide: ENDPOINT_URL, useValue: environment.endpointUrl },
    { provide: LOGIN_URL, useValue: '/login' }
  ],
  bootstrap: [AppComponent]
})
export class AppModule {}
