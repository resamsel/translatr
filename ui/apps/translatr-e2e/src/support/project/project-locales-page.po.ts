import type { Locator, Page } from '@playwright/test';
import { PageObject } from '../page.po';

export class ProjectLocalesPage extends PageObject {
  constructor(
    page: Page,
    private readonly username: string,
    private readonly projectName: string,
  ) {
    super(page);
  }

  async navigateTo(): Promise<ProjectLocalesPage> {
    await this.page.goto(`${this.username}/${this.projectName}/locales`);
    return this;
  }

  getLocaleList(): Locator {
    return this.page.locator('app-locale-list mat-nav-list');
  }
}
