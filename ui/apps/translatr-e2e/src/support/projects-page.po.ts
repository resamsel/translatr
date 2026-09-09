import type { Locator } from '@playwright/test';
import { PageObject } from './page.po';

export class ProjectsPage extends PageObject {
  async navigateTo(): Promise<ProjectsPage> {
    await this.page.goto('projects');
    return this;
  }

  getCards(): Locator {
    return this.page.locator('app-project-list a[mat-list-item]');
  }

  getSearchField(): Locator {
    return this.page.locator('app-project-list dev-filter-field input');
  }

  getEmptyView(): Locator {
    return this.page.locator('app-project-list dev-empty-view');
  }
}
