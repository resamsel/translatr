import type { Locator, Page } from '@playwright/test';
import { PageObject } from '../page.po';

export class UserAccessTokenPage extends PageObject {
  constructor(
    page: Page,
    private readonly username: string,
    private readonly id: string = 'create',
  ) {
    super(page);
  }

  async navigateTo(): Promise<UserAccessTokenPage> {
    await this.page.goto(`${this.username}/access-tokens/${this.id}`);
    return this;
  }

  getNameField(): Locator {
    return this.page.locator('input[formcontrolname="name"]');
  }

  getSaveButton(): Locator {
    return this.page.locator('button[transloco="button.save"]');
  }

  getCancelLink(): Locator {
    return this.page.locator('a[transloco="button.cancel"]');
  }

  getSecret(): Locator {
    return this.page.locator('.key input[formcontrolname="key"]');
  }
}
