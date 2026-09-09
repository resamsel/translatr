import type { Page } from '@playwright/test';
import { PageObject } from '../page.po';

export class UserProjectsPage extends PageObject {
  constructor(page: Page, private readonly username: string) {
    super(page);
  }

  async navigateTo(): Promise<UserProjectsPage> {
    await this.page.goto(`${this.username}/projects`);
    return this;
  }
}
