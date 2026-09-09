import type { Locator, Page } from '@playwright/test';
import { PageObject } from '../page.po';

export class UserActivityPage extends PageObject {
  constructor(page: Page, private readonly username: string) {
    super(page);
  }

  async navigateTo(): Promise<UserActivityPage> {
    await this.page.goto(`${this.username}/activity`);
    return this;
  }

  getActivityRows(): Locator {
    return this.page.locator('app-activity-list mat-list-item');
  }

  getUserLink(): Locator {
    return this.page.locator('app-activity-list a.user-name');
  }

  getLoadMoreButton(): Locator {
    return this.page.locator('app-activity-list a.more');
  }
}
