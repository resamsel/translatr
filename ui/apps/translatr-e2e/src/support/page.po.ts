import type { Locator, Page } from '@playwright/test';

/** Base page object: chrome shared across every translatr screen. */
export class PageObject {
  constructor(protected readonly page: Page) {}

  getPageName(): Locator {
    return this.page.locator('.page');
  }

  getTitle(): Locator {
    return this.page.locator('title');
  }

  getPageTitle(): Locator {
    return this.page.locator('.title > span');
  }

  getFloatingActionButton(): Locator {
    return this.page.locator('.floating-action-btn');
  }

  getDialog(): Locator {
    return this.page.locator('mat-dialog-container');
  }
}
