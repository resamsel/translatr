/* tslint:disable:max-classes-per-file */
import { HttpClient } from '@angular/common/http';
import { Injectable, NgModule } from '@angular/core';
import { LanguageProvider } from '@dev/translatr-sdk';
import {
  Translation,
  TRANSLOCO_CONFIG,
  TRANSLOCO_LOADER,
  translocoConfig,
  TranslocoLoader,
  TranslocoModule,
  TranslocoService
} from '@ngneat/transloco';
import { TranslocoMessageFormatModule } from '@ngneat/transloco-messageformat';
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
  imports: [TranslocoModule, TranslocoMessageFormatModule.init()],
  exports: [TranslocoModule, TranslocoMessageFormatModule],
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
    { provide: LanguageProvider, useClass: TranslocoLanguageProvider }
  ]
})
export class TranslocoRootModule {}
