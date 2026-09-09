import type { Locator } from '@playwright/test';
import { PageObject } from './page.po';

export class FeatureFlagsPage extends PageObject {
  /** The tabbed Feature Flags page opens on the "User" tab. */
  async navigateTo(): Promise<FeatureFlagsPage> {
    await this.page.goto('featureflags');
    return this;
  }

  /** Open the page and switch to the "Global" tab. */
  async navigateToGlobal(): Promise<FeatureFlagsPage> {
    await this.navigateTo();
    await this.selectTab('Global');
    return this;
  }

  async selectTab(label: string): Promise<FeatureFlagsPage> {
    await this.page.getByRole('tab', { name: label }).click();
    return this;
  }

  getActiveTab(): Locator {
    return this.page.locator('[role="tab"][aria-selected="true"]');
  }

  getGlobalDefaultLine(featureKey: string): Locator {
    return this.getRow(featureKey).locator('.feature-name .sub-title').last();
  }

  getRows(): Locator {
    return this.page.locator('.feature-row');
  }

  /**
   * Matches a feature row on the raw Feature enum value rendered in
   * `.feature-name .sub-title` (e.g. `language-switcher`).
   */
  getRow(featureKey: string): Locator {
    return this.getRows().filter({ hasText: featureKey });
  }

  getToggle(featureKey: string): Locator {
    return this.getRow(featureKey).locator('.feature-actions button');
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
