import type { Locator, Page } from '@playwright/test';
import { PageObject } from '../page.po';
import { ProjectPage } from './project-page.po';

export class ProjectSettingsPage extends PageObject {
  constructor(
    page: Page,
    private readonly username: string,
    private readonly projectName: string,
  ) {
    super(page);
  }

  async navigateTo(): Promise<ProjectSettingsPage> {
    await this.page.goto(`${this.username}/${this.projectName}/settings`);
    return this;
  }

  async navigateToProjectPage(): Promise<ProjectPage> {
    return new ProjectPage(this.page, this.username, this.projectName).navigateTo();
  }

  getNameField(): Locator {
    return this.page.locator('mat-form-field.name input');
  }

  getNameFieldError(): Locator {
    return this.page.locator('mat-form-field.name mat-error');
  }

  getDescriptionField(): Locator {
    return this.page.locator('mat-form-field.description textarea');
  }

  getSaveButton(): Locator {
    return this.page.locator('.update-project button.save');
  }

  getDeleteProjectButton(): Locator {
    return this.page.locator('.delete-project button.delete');
  }

  getDeleteProjectDialog(): Locator {
    return this.page.locator('app-project-delete-dialog');
  }

  getCancelProjectDeleteDialogButton(): Locator {
    return this.page.locator('app-project-delete-dialog button.cancel');
  }
}
