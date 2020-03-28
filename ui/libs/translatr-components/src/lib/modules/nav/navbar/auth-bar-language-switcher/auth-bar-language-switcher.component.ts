import { Component, EventEmitter, Output } from '@angular/core';
import { TranslocoService } from '@ngneat/transloco';

@Component({
  selector: 'app-auth-bar-language-switcher',
  templateUrl: './auth-bar-language-switcher.component.html',
  styleUrls: ['./auth-bar-language-switcher.component.css']
})
export class AuthBarLanguageSwitcherComponent {
  @Output() readonly languageSwitch = new EventEmitter<string>();
  readonly availableLanguages = this.translocoService.getAvailableLangs();
  readonly activeLang = this.translocoService.getActiveLang();

  constructor(private readonly translocoService: TranslocoService) {
  }

  onSwitchLanguage(language: string): void {
    this.translocoService.setActiveLang(language);
    this.languageSwitch.emit(language);
  }
}
