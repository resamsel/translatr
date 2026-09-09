import type { Locator, Page } from '@playwright/test';
import { PageObject } from '../page.po';

export class ProjectKeysPage extends PageObject {
  constructor(
    page: Page,
    private readonly username: string,
    private readonly projectName: string,
  ) {
    super(page);
  }

  async navigateTo(): Promise<ProjectKeysPage> {
    await this.page.goto(`${this.username}/${this.projectName}/keys`);
    return this;
  }

  getKeyList(): Locator {
    return this.page.locator('app-key-list mat-nav-list');
  }
}
