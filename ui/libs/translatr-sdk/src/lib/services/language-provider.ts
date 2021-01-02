import { Injectable } from '@angular/core';
@Injectable()
export class LanguageProvider {
  getActiveLang(): string {
    return 'en';
  }
}
