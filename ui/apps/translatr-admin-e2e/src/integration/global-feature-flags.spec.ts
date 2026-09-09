import { test, expect } from '../support/test';
import { mockApi, waitForApi } from '../support/mock-api';
import { FeatureFlagsPage } from '../support/feature-flags-page.po';

test.describe('Admin Global Feature Flags', () => {
  test.beforeEach(async ({ page }) => {
    await mockApi(page, '/api/featureflags/resolved', 'resolved-features');
    await mockApi(page, '/api/featureflags/global', 'global-feature-flags');
  });

  test('renders one row per known feature with its default', async ({ page }) => {
    const flags = await new FeatureFlagsPage(page).navigateToGlobal();

    await expect(flags.getPageName()).toHaveText('Feature Flags');
    await expect(flags.getActiveTab()).toContainText('Global');
    await expect(flags.getRows()).toHaveCount(4);
    await expect(flags.getToggle('header-graphic').locator('mat-icon')).toHaveText('toggle_on');
    await expect(flags.getToggle('language-switcher').locator('mat-icon')).toHaveText('toggle_off');
  });

  test('POSTs feature + enabled when toggling a feature on globally', async ({ page }) => {
    await mockApi(page, '/api/featureflag/global', 'global-feature-flag-set', { method: 'POST' });
    const flags = await new FeatureFlagsPage(page).navigateToGlobal();

    const set = waitForApi(page, '/api/featureflag/global', 'POST');
    await flags.getToggle('language-switcher').click();

    expect((await set).postDataJSON()).toEqual({ feature: 'language-switcher', enabled: true });
  });

  test('POSTs enabled=false when disabling a globally-enabled feature', async ({ page }) => {
    await mockApi(
      page,
      '/api/featureflag/global',
      { id: 'a0000000-0000-0000-0000-000000000001', feature: 'header-graphic', enabled: false },
      { method: 'POST' },
    );
    const flags = await new FeatureFlagsPage(page).navigateToGlobal();

    const set = waitForApi(page, '/api/featureflag/global', 'POST');
    await flags.getToggle('header-graphic').click();

    expect((await set).postDataJSON()).toEqual({ feature: 'header-graphic', enabled: false });
  });
});
