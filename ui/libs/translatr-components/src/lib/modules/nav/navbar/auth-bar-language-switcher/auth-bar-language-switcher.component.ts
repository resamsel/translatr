import { Component, Optional } from '@angular/core';
import { TranslocoService } from '@ngneat/transloco';
import { LanguageSwicher } from '../language-swicher';

@Component({
  selector: 'app-auth-bar-language-switcher',
  templateUrl: './auth-bar-language-switcher.component.html',
  styleUrls: ['./auth-bar-language-switcher.component.css']
})
export class AuthBarLanguageSwitcherComponent {
  readonly availableLanguages = this.translocoService.getAvailableLangs();
  activeLang = this.translocoService.getActiveLang();

  constructor(
    private readonly translocoService: TranslocoService,
    @Optional() private readonly languageSwicher: LanguageSwicher
  ) {}

  onSwitchLanguage(language: string): void {
    if (this.activeLang !== language) {
      this.translocoService.setActiveLang(language);
      this.activeLang = this.translocoService.getActiveLang();
      if (this.languageSwicher !== null) {
        this.languageSwicher.updatePreferredLanguage(language);
      }
    }
  }
}
