import type { Locator } from '@playwright/test';
import { PageObject } from './page.po';

export class DashboardPage extends PageObject {
  async navigateTo(): Promise<DashboardPage> {
    await this.page.goto('');
    return this;
  }

  getMetric(cls: string): Locator {
    return this.page.locator(`dev-metric.${cls}.count`);
  }
}
