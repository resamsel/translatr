import type { Locator } from '@playwright/test';
import { DashboardPage } from './dashboard.po';
import { PageObject } from './page.po';

export class HomePage extends PageObject {
  async navigateTo(): Promise<HomePage> {
    await this.page.goto('');
    return this;
  }

  getPageName(): Locator {
    return this.page.locator('h1');
  }

  goToDashboard(): Promise<DashboardPage> {
    return new DashboardPage(this.page).navigateTo();
  }

  getProjectMetricValue(): Locator {
    return this.page.locator('dev-metric.project mat-card-title');
  }

  getUserMetricValue(): Locator {
    return this.page.locator('dev-metric.user mat-card-title');
  }

  getActivityMetricValue(): Locator {
    return this.page.locator('dev-metric.activity mat-card-title');
  }
}
