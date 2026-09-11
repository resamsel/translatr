import type { Locator } from '@playwright/test';
import { PageObject } from './page.po';

export class FeatureFlagsPage extends PageObject {
  /** The personal "Feature Flags" page. */
  async navigateTo(): Promise<FeatureFlagsPage> {
    await this.page.goto('featureflags');
    return this;
  }

  /** The "Global Feature Flags" page — a separate top-level page, not a tab. */
  async navigateToGlobal(): Promise<FeatureFlagsPage> {
    await this.page.goto('featureflags/global');
    return this;
  }

  getGlobalDefaultCell(featureKey: string): Locator {
    return this.getRow(featureKey).locator('.mat-column-globalDefault');
  }

  getRows(): Locator {
    return this.page.locator('table tr[mat-row]');
  }

  /**
   * Matches a feature row on the raw Feature enum value rendered in the
   * "Feature" column's sub-title (e.g. `language-switcher`).
   */
  getRow(featureKey: string): Locator {
    return this.getRows().filter({ hasText: featureKey });
  }

  getToggle(featureKey: string): Locator {
    return this.getRow(featureKey).getByRole('switch');
  }

  getUserPickerInput(): Locator {
    return this.page.locator('.user-picker input');
  }

  async pickUser(search: string): Promise<FeatureFlagsPage> {
    await this.getUserPickerInput().fill(search);
    await this.page.locator('mat-option').filter({ hasText: search }).click();
    return this;
  }
}
