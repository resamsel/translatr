import type { Locator, Page } from '@playwright/test';
import { PageObject } from '../page.po';

export class UserAccessTokensPage extends PageObject {
  constructor(page: Page, private readonly username: string) {
    super(page);
  }

  async navigateTo(): Promise<UserAccessTokensPage> {
    await this.page.goto(`${this.username}/access-tokens`);
    return this;
  }

  getRows(): Locator {
    return this.page.locator('app-user-access-tokens a[mat-list-item]');
  }

  getEmptyView(): Locator {
    return this.page.locator('app-user-access-tokens dev-empty-view');
  }
}
