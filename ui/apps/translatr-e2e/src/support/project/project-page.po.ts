import type { Locator, Page } from '@playwright/test';
import { PageObject } from '../page.po';
import { ProjectSettingsPage } from './project-settings-page.po';

export class ProjectPage extends PageObject {
  constructor(
    page: Page,
    public readonly username: string,
    public readonly projectName: string,
  ) {
    super(page);
  }

  async navigateTo(): Promise<ProjectPage> {
    await this.page.goto(`${this.username}/${this.projectName}`);
    return this;
  }

  async navigateToSettings(): Promise<ProjectSettingsPage> {
    return new ProjectSettingsPage(this.page, this.username, this.projectName).navigateTo();
  }

  getDescription(): Locator {
    return this.page.locator('p.description');
  }
}
