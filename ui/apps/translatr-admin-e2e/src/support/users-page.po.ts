import type { Locator } from '@playwright/test';
import { PageObject } from './page.po';

export class UsersPage extends PageObject {
  async navigateTo(): Promise<UsersPage> {
    await this.page.goto('users');
    return this;
  }

  getRows(): Locator {
    return this.page.locator('entity-table tbody tr');
  }

  getRow(name: string): Locator {
    return this.getRows().filter({ hasText: name });
  }

  getSearchField(): Locator {
    return this.page.locator('entity-table dev-filter-field input');
  }

  getEditButton(name: string): Locator {
    return this.getRow(name).locator('button').first();
  }
}
