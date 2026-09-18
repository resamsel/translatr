import { Component, Optional, ChangeDetectionStrategy } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { LanguageSwicher } from '../language-swicher';

@Component({
  standalone: true,
  selector: 'app-auth-bar-language-switcher',
  templateUrl: './auth-bar-language-switcher.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./auth-bar-language-switcher.component.css'],
  imports: [MatButtonModule, MatMenuModule, MatIconModule, TranslocoModule]
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
