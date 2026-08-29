import { LayoutModule } from '@angular/cdk/layout';
import { provideHttpClient, withInterceptorsFromDi, withXhr } from '@angular/common/http';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatToolbarModule } from '@angular/material/toolbar';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { FeatureFlagModule } from '@dev/translatr-components';
import { FeatureFlagFacade } from '@dev/translatr-model';
import { NotificationService, TranslatrSdkModule } from '@dev/translatr-sdk';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { HotkeysService } from '@ngneat/hotkeys';
import { SvgIconComponent, provideSvgIcons } from '@ngneat/svg-icon';
import { EffectsModule } from '@ngrx/effects';
import { routerReducer, RouterState, StoreRouterConnectingModule } from '@ngrx/router-store';
import { StoreModule } from '@ngrx/store';
import { StoreDevtoolsModule } from '@ngrx/store-devtools';
import { ENDPOINT_URL, LOGIN_URL, WINDOW } from '@translatr/utils';
import { environment } from '../environments/environment';
import { AppEffects } from './+state/app.effects';
import { AppFacade } from './+state/app.facade';
import { appReducer } from './+state/app.reducer';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { httpInterceptorProviders } from './interceptors';
import { TranslocoRootModule } from './modules/shared/transloco';
import { MatNotificationService } from './services/mat-notification-service';

@NgModule({
  declarations: [AppComponent],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,

    AppRoutingModule,
    TranslatrSdkModule,
    LayoutModule,
    FeatureFlagModule,

    MatToolbarModule,
    MatButtonModule,
    MatSnackBarModule,
    MatDialogModule,

    StoreModule.forRoot(
      {
        app: appReducer,
        router: routerReducer
      },
      {
        metaReducers: [],
        runtimeChecks: {
          strictStateImmutability: true,
          strictActionImmutability: true
        }
      }
    ),
    EffectsModule.forRoot([AppEffects]),
    !environment.production ? StoreDevtoolsModule.instrument() : [],
    StoreRouterConnectingModule.forRoot({ routerState: RouterState.Minimal }),
    TranslocoRootModule,
    FontAwesomeModule,
    SvgIconComponent
  ],
  providers: [
    provideHttpClient(withXhr(), withInterceptorsFromDi()),
    AppFacade,
    { provide: FeatureFlagFacade, useClass: AppFacade },
    { provide: WINDOW, useFactory: () => window },
    { provide: ENDPOINT_URL, useValue: environment.endpointUrl },
    { provide: LOGIN_URL, useValue: `/login` },
    {
      provide: NotificationService,
      useFactory: (snackBar: MatSnackBar) => new MatNotificationService(snackBar),
      deps: [MatSnackBar]
    },
    httpInterceptorProviders,
    HotkeysService,
    provideSvgIcons([])
  ],
  bootstrap: [AppComponent]
})
export class AppModule {}
