import type { Locator } from '@playwright/test';
import { PageObject } from './page.po';

export class UsersPage extends PageObject {
  async navigateTo(): Promise<UsersPage> {
    await this.page.goto('users');
    return this;
  }

  getRows(): Locator {
    return this.page.locator('app-user-list a[mat-list-item]');
  }

  getSearchField(): Locator {
    return this.page.locator('app-user-list dev-filter-field input');
  }

  getEmptyView(): Locator {
    return this.page.locator('app-user-list dev-empty-view');
  }

  getLoadMoreButton(): Locator {
    return this.page.locator('app-user-list a.more');
  }
}
