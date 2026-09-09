import type { Locator } from '@playwright/test';
import { PageObject } from './page.po';

export class DashboardPage extends PageObject {
  async navigateTo(): Promise<DashboardPage> {
    await this.page.goto('dashboard');
    return this;
  }

  getProjectCardLinks(): Locator {
    return this.page.locator('app-project-card-link');
  }

  getProjectEmptyView(): Locator {
    return this.page.locator('app-project-empty-view');
  }

  getMetric(kind: string): Locator {
    return this.page.locator(`dev-metric.${kind}.count`);
  }

  getProjectCreationDialog(): Locator {
    return this.page.locator('app-protect-creation-dialog');
  }
}
