import type { Locator, Page } from '@playwright/test';

/** Base page object: common chrome shared by every admin screen. */
export class PageObject {
  constructor(protected readonly page: Page) {}

  /**
   * Most pages only show their title via the navbar's page label
   * (`app-navbar .page`); a page with its own dynamic title in the body
   * (e.g. the user detail page's `h1.page` showing the user's name) renders
   * that instead and leaves the navbar label empty — so match either.
   */
  getPageName(): Locator {
    return this.page.locator('app-navbar .page, h1.page');
  }

  getFloatingActionButton(): Locator {
    return this.page.locator('.floating-action-btn');
  }

  getDialog(): Locator {
    return this.page.locator('mat-dialog-container');
  }
}
