import { provideHttpClient, withInterceptorsFromDi, withXhr } from '@angular/common/http';
import { enableProdMode, importProvidersFrom, provideZoneChangeDetection } from '@angular/core';
import { bootstrapApplication } from '@angular/platform-browser';
import { provideAnimations } from '@angular/platform-browser/animations';
import { provideRouter } from '@angular/router';
import { FeatureFlagFacade } from '@dev/translatr-model';
import { TranslatrSdkModule } from '@dev/translatr-sdk';
import { EffectsModule } from '@ngrx/effects';
import { provideRouterStore, routerReducer, RouterState } from '@ngrx/router-store';
import { StoreModule } from '@ngrx/store';
import { provideStoreDevtools } from '@ngrx/store-devtools';
import { ENDPOINT_URL, LOGIN_URL, WINDOW } from '@translatr/utils';
import { environment } from './environments/environment';
import { AppEffects } from './app/+state/app.effects';
import { AppFacade } from './app/+state/app.facade';
import { appReducer } from './app/+state/app.reducer';
import { routes } from './app/app-routing.module';
import { AppComponent } from './app/app.component';
import { DashboardPageRoutingModule } from './app/modules/pages/dashboard-page/dashboard-page-routing.module';
import { TranslocoRootModule } from './app/modules/transloco';

if (environment.production) {
  enableProdMode();
}

bootstrapApplication(AppComponent, {
  providers: [
    provideZoneChangeDetection(),
    provideAnimations(),
    provideRouter(routes),
    provideHttpClient(withXhr(), withInterceptorsFromDi()),
    // Mirrors apps/translatr's root-bootstrap: StoreModule.forFeature()/EffectsModule.forFeature()
    // used elsewhere (via DashboardPageRoutingModule below) require the NgModule forRoot() form -
    // provideStore()/provideEffects() don't provide the StoreRootModule/EffectsRootModule marker
    // those factories inject, so mixing them throws NG0201.
    importProvidersFrom(
      StoreModule.forRoot(
        { app: appReducer, router: routerReducer },
        {
          metaReducers: [],
          runtimeChecks: {
            strictStateImmutability: true,
            strictActionImmutability: true
          }
        }
      )
    ),
    importProvidersFrom(EffectsModule.forRoot([AppEffects])),
    provideRouterStore({ routerState: RouterState.Minimal }),
    ...(!environment.production ? [provideStoreDevtools()] : []),
    importProvidersFrom(TranslatrSdkModule),
    // Registers the 8 dashboard routes (RouterModule.forChild) and the DASHBOARD_ROUTES token
    // AdminPageComponent injects for its nav list - same eager root-level route composition
    // AppModule used (RouterModule.forRoot + a nested RouterModule.forChild both contributing
    // to the same injector).
    importProvidersFrom(DashboardPageRoutingModule),
    importProvidersFrom(TranslocoRootModule),
    AppFacade,
    { provide: FeatureFlagFacade, useClass: AppFacade },
    { provide: WINDOW, useFactory: () => window },
    { provide: ENDPOINT_URL, useValue: environment.endpointUrl },
    { provide: LOGIN_URL, useValue: `${environment.uiUrl}/login` }
  ]
}).catch(err => console.error(err));
