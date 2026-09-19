import { provideHttpClient, withInterceptorsFromDi, withXhr } from '@angular/common/http';
import { enableProdMode, importProvidersFrom, provideZoneChangeDetection } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { bootstrapApplication } from '@angular/platform-browser';
import { provideAnimations } from '@angular/platform-browser/animations';
import { provideRouter, withDebugTracing } from '@angular/router';
import { FeatureFlagFacade } from '@dev/translatr-model';
import { NotificationService, TranslatrSdkModule } from '@dev/translatr-sdk';
import { HotkeysService } from '@ngneat/hotkeys';
import { provideSvgIcons } from '@ngneat/svg-icon';
import { provideEffects } from '@ngrx/effects';
import { provideRouterStore, routerReducer, RouterState } from '@ngrx/router-store';
import { provideStore } from '@ngrx/store';
import { provideStoreDevtools } from '@ngrx/store-devtools';
import { ENDPOINT_URL, LOGIN_URL, WINDOW } from '@translatr/utils';
import { AppEffects } from './app/+state/app.effects';
import { AppFacade } from './app/+state/app.facade';
import { appReducer } from './app/+state/app.reducer';
import { routes } from './app/app-routing.module';
import { AppComponent } from './app/app.component';
import { httpInterceptorProviders } from './app/interceptors';
import { TranslocoRootModule } from './app/modules/shared/transloco';
import { MatNotificationService } from './app/services/mat-notification-service';
import { environment } from './environments/environment';

if (environment.production) {
  enableProdMode();
}

bootstrapApplication(AppComponent, {
  providers: [
    provideZoneChangeDetection(),
    provideAnimations(),
    provideRouter(routes, ...(environment.routerTracing ? [withDebugTracing()] : [])),
    provideHttpClient(withXhr(), withInterceptorsFromDi()),
    provideStore(
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
    provideEffects([AppEffects]),
    provideRouterStore({ routerState: RouterState.Minimal }),
    ...(!environment.production ? [provideStoreDevtools()] : []),
    importProvidersFrom(TranslocoRootModule),
    importProvidersFrom(TranslatrSdkModule),
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
  ]
}).catch(err => console.error(err));
