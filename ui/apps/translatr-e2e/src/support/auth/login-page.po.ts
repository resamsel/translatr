import type { Locator } from '@playwright/test';
import { PageObject } from '../page.po';

export class LoginPage extends PageObject {
  async navigateTo(): Promise<LoginPage> {
    await this.page.goto('login');
    return this;
  }

  getProviderLinks(): Locator {
    return this.page.locator('.options a.client');
  }
}
