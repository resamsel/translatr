import type { Locator, Page } from '@playwright/test';
import { PageObject } from '../page.po';

export class UserSettingsPage extends PageObject {
  constructor(page: Page, private readonly username: string) {
    super(page);
  }

  async navigateTo(): Promise<UserSettingsPage> {
    await this.page.goto(`${this.username}/settings`);
    return this;
  }

  getNameField(): Locator {
    return this.page.locator('mat-form-field.name input');
  }

  getNameFieldError(): Locator {
    return this.page.locator('mat-form-field.name mat-error');
  }

  getUsernameField(): Locator {
    return this.page.locator('mat-form-field.username input');
  }

  getUsernameFieldError(): Locator {
    return this.page.locator('mat-form-field.username mat-error');
  }

  getSaveButton(): Locator {
    return this.page.locator('button.save');
  }
}
