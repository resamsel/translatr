import type { Locator, Page } from '@playwright/test';

/** Base page object: common chrome shared by every admin screen. */
export class PageObject {
  constructor(protected readonly page: Page) {}

  getPageName(): Locator {
    return this.page.locator('h1.page');
  }

  getFloatingActionButton(): Locator {
    return this.page.locator('.floating-action-btn');
  }

  getDialog(): Locator {
    return this.page.locator('mat-dialog-container');
  }
}
