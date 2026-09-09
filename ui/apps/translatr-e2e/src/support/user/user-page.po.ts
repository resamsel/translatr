import type { Locator, Page } from '@playwright/test';
import { PageObject } from '../page.po';

export class UserPage extends PageObject {
  constructor(page: Page, private readonly username: string) {
    super(page);
  }

  async navigateTo(): Promise<UserPage> {
    await this.page.goto(this.username);
    return this;
  }

  getName(): Locator {
    return this.page.locator('.name');
  }
}
