import type { Locator } from '@playwright/test';
import { PageObject } from './page.po';

export class ProjectsPage extends PageObject {
  async navigateTo(): Promise<ProjectsPage> {
    await this.page.goto('projects');
    return this;
  }

  getRows(): Locator {
    return this.page.locator('entity-table tbody tr');
  }

  getSearchField(): Locator {
    return this.page.locator('entity-table dev-filter-field input');
  }
}
