import type { Locator } from '@playwright/test';
import { PageObject } from '../page.po';

export class NavbarPage extends PageObject {
  async navigateTo(): Promise<NavbarPage> {
    await this.page.goto('dashboard');
    return this;
  }

  getLanguageSwitcher(): Locator {
    return this.page.locator('app-auth-bar-language-switcher');
  }

  getLanguageSwitcherTrigger(): Locator {
    return this.page.locator('app-auth-bar-language-switcher button');
  }

  getUserMenuTrigger(): Locator {
    return this.page.locator('app-auth-bar-item button.account-button');
  }

  getMenuPanel(): Locator {
    return this.page.locator('.mat-mdc-menu-panel');
  }
}
