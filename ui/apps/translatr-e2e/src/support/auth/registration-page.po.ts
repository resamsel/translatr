import type { Locator } from '@playwright/test';
import { PageObject } from '../page.po';

export class RegistrationPage extends PageObject {
  async navigateTo(): Promise<RegistrationPage> {
    await this.page.goto('register');
    return this;
  }

  getNameField(): Locator {
    return this.page.locator('input[formcontrolname="name"]');
  }

  getUsernameField(): Locator {
    return this.page.locator('input[formcontrolname="username"]');
  }

  getUsernameError(): Locator {
    return this.page.locator('dev-user-edit-form mat-error');
  }

  getSubmitButton(): Locator {
    return this.page.locator('button[transloco="button.save"]');
  }

  async fillName(value: string): Promise<RegistrationPage> {
    await this.getNameField().click({ position: { x: 240, y: 15 } });
    await this.getNameField().fill(value);
    return this;
  }

  async fillUsername(value: string): Promise<RegistrationPage> {
    await this.getUsernameField().click({ position: { x: 240, y: 15 } });
    await this.getUsernameField().fill(value);
    await this.getUsernameField().blur();
    return this;
  }
}
