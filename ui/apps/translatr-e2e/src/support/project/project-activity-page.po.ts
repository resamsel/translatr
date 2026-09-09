import type { Locator, Page } from '@playwright/test';
import { PageObject } from '../page.po';

export class ProjectActivityPage extends PageObject {
  constructor(
    page: Page,
    private readonly username: string,
    private readonly projectName: string,
  ) {
    super(page);
  }

  async navigateTo(): Promise<ProjectActivityPage> {
    await this.page.goto(`${this.username}/${this.projectName}/activity`);
    return this;
  }

  getActivityRows(): Locator {
    return this.page.locator('app-activity-list mat-list-item');
  }

  getActivityGraph(): Locator {
    return this.page.locator('dev-activity-graph');
  }
}
