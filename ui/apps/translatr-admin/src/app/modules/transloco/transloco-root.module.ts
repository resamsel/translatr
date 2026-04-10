import { HttpClient } from '@angular/common/http';
import { Injectable, NgModule } from '@angular/core';
import { LanguageProvider } from '@dev/translatr-sdk';
import {
  DefaultFallbackStrategy,
  DefaultInterceptor,
  DefaultMissingHandler,
  Translation,
  TRANSLOCO_CONFIG,
  TRANSLOCO_FALLBACK_STRATEGY,
  TRANSLOCO_INTERCEPTOR,
  TRANSLOCO_LOADER,
  TRANSLOCO_MISSING_HANDLER,
  translocoConfig,
  TranslocoLoader,
  TranslocoModule,
  TranslocoService
} from '@jsverse/transloco';
import { provideTranslocoMessageformat } from '@jsverse/transloco-messageformat';
import { environment } from '../../../environments/environment';

@Injectable()
export class TranslocoHttpLoader implements TranslocoLoader {
  constructor(private http: HttpClient) {}

  getTranslation(lang: string) {
    return this.http.get<Translation>(`./assets/i18n/${lang}.json`);
  }
}

@Injectable()
export class TranslocoLanguageProvider extends LanguageProvider {
  constructor(private translocoService: TranslocoService) {
    super();
  }

  getActiveLang(): string {
    return this.translocoService.getActiveLang();
  }
}

@NgModule({
  imports: [TranslocoModule],
  exports: [TranslocoModule],
  providers: [
    {
      provide: TRANSLOCO_CONFIG,
      useValue: translocoConfig({
        availableLangs: ['en', 'de'],
        defaultLang: 'en',
        fallbackLang: 'en',
        missingHandler: {
          useFallbackTranslation: true
        },
        reRenderOnLangChange: true,
        prodMode: environment.production
      })
    },
    { provide: TRANSLOCO_LOADER, useClass: TranslocoHttpLoader },
    { provide: TRANSLOCO_MISSING_HANDLER, useClass: DefaultMissingHandler },
    { provide: TRANSLOCO_INTERCEPTOR, useClass: DefaultInterceptor },
    { provide: TRANSLOCO_FALLBACK_STRATEGY, useClass: DefaultFallbackStrategy, deps: [TRANSLOCO_CONFIG] },
    { provide: LanguageProvider, useClass: TranslocoLanguageProvider },
    provideTranslocoMessageformat()
  ]
})
export class TranslocoRootModule {}
