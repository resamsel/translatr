import { HttpClient } from '@angular/common/http';
import { Translation, TRANSLOCO_CONFIG, TRANSLOCO_LOADER, translocoConfig, TranslocoLoader, TranslocoModule } from '@ngneat/transloco';
import { Injectable, ModuleWithProviders, NgModule } from '@angular/core';

@Injectable({providedIn: 'root'})
export class TranslocoHttpLoader implements TranslocoLoader {
  constructor(private http: HttpClient) {
  }

  getTranslation(lang: string) {
    return this.http.get<Translation>(`./assets/i18n/${lang}.json`);
  }
}

@NgModule({
  exports: [TranslocoModule],
  providers: [
    {provide: TRANSLOCO_LOADER, useClass: TranslocoHttpLoader}
  ]
})
export class TranslocoRootModule {
  static forRoot(prodMode: boolean): ModuleWithProviders {
    return {
      ngModule: TranslocoRootModule,
      providers: [
        {
          provide: TRANSLOCO_CONFIG,
          useValue: translocoConfig({
            availableLangs: ['en', 'de'],
            defaultLang: 'en',
            // Remove this option if your application doesn't support changing language in runtime.
            reRenderOnLangChange: true,
            prodMode: prodMode,
          })
        },
      ]
    };
  }
}
