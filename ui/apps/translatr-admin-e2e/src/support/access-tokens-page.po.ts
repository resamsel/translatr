import type { Locator } from '@playwright/test';
import { PageObject } from './page.po';

export class AccessTokensPage extends PageObject {
  async navigateTo(): Promise<AccessTokensPage> {
    await this.page.goto('accesstokens');
    return this;
  }

  getRows(): Locator {
    return this.page.locator('entity-table tbody tr');
  }

  getEditButton(name: string): Locator {
    return this.getRows().filter({ hasText: name }).locator('button').first();
  }
}
