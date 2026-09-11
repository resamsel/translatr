import { test, expect } from '../support/test';
import { loadFixture, mockApi, mockApiWith, waitForApi } from '../support/mock-api';
import { FeatureFlagsPage } from '../support/feature-flags-page.po';

test.describe('Admin Feature Flags', () => {
  test.beforeEach(async ({ page }) => {
    await mockApi(page, '/api/featureflags/resolved', 'resolved-features');
  });

  test('renders one row per known feature with its global-default line', async ({ page }) => {
    const flags = await new FeatureFlagsPage(page).navigateTo();

    await expect(flags.getPageName()).toHaveText('Feature Flags');
    await expect(flags.getRows()).toHaveCount(4);
    await expect(flags.getGlobalDefaultCell('header-graphic')).toContainText('on');
    await expect(flags.getGlobalDefaultCell('project-cli-card')).toContainText('off');
  });

  test('CREATE: toggling a feature with no override, away from the default, POSTs enabled=true', async ({
    page,
  }) => {
    await mockApi(page, '/api/featureflag', 'feature-flag-created', { method: 'POST' });
    const flags = await new FeatureFlagsPage(page).navigateTo();

    const create = waitForApi(page, '/api/featureflag', 'POST');
    await flags.getToggle('project-infographic').click();

    expect((await create).postDataJSON()).toMatchObject({
      feature: 'project-infographic',
      enabled: true,
    });
  });

  test('DELETE: toggling an override back to the global default removes the row', async ({
    page,
  }) => {
    // After the override is deleted the app re-fetches /api/featureflags/resolved;
    // that reload must reflect language-switcher back at its global default (off).
    let overrideRemoved = false;
    await mockApiWith(
      page,
      '/api/featureflags/resolved',
      async (route) => {
        await route.fulfill({
          json: overrideRemoved
            ? [
                { feature: 'project-cli-card', defaultEnabled: false, global: null, userOverride: null, userOverrideId: null, effective: false },
                { feature: 'project-infographic', defaultEnabled: false, global: null, userOverride: null, userOverrideId: null, effective: false },
                { feature: 'header-graphic', defaultEnabled: false, global: true, userOverride: null, userOverrideId: null, effective: true },
                { feature: 'language-switcher', defaultEnabled: false, global: null, userOverride: null, userOverrideId: null, effective: false },
              ]
            : loadFixture('resolved-features'),
        });
      },
      { method: 'GET' },
    );

    await mockApiWith(
      page,
      '/api/featureflag/f1000000-0000-0000-0000-000000000001',
      async (route) => {
        overrideRemoved = true;
        await route.fulfill({ json: { id: 'f1000000-0000-0000-0000-000000000001' } });
      },
      { method: 'DELETE' },
    );

    const flags = await new FeatureFlagsPage(page).navigateTo();

    const remove = waitForApi(page, '/api/featureflag/f1000000-0000-0000-0000-000000000001', 'DELETE');
    // language-switcher: override ON, global default OFF -> toggling off returns to default
    await flags.getToggle('language-switcher').click();

    expect((await remove).method()).toBe('DELETE');
    await expect(flags.getToggle('language-switcher')).not.toBeChecked();
  });

  // The whole admin app is already gated to Admin users by AuthGuard (a non-admin never
  // reaches this page at all - see auth.guard.ts), so every scenario here runs as the
  // fixture's admin.
  test.describe("managing another user's flags", () => {
    test.beforeEach(async ({ page }) => {
      await mockApi(page, '/api/user*', 'users');
    });

    test('an admin can pick another user, which updates the URL to a sharable link', async ({
      page,
    }) => {
      await mockApi(page, '/api/featureflags/resolved*', 'resolved-features', { method: 'GET' });
      const flags = await new FeatureFlagsPage(page).navigateTo();

      const resolved = waitForApi(page, '/api/featureflags/resolved*userId=*', 'GET');
      await flags.pickUser('janesmith');

      await expect(page).toHaveURL(/userId=5e15a05d-c583-45a0-84fa-1e770b2a4532/);
      expect((await resolved).url()).toContain('userId=5e15a05d-c583-45a0-84fa-1e770b2a4532');
    });

    test("opening a shared ?userId= link loads that user's flags directly", async ({ page }) => {
      await mockApi(page, '/api/featureflags/resolved*', 'resolved-features', { method: 'GET' });
      await mockApi(page, '/api/user/5e15a05d-c583-45a0-84fa-1e770b2a4532*', {
        id: '5e15a05d-c583-45a0-84fa-1e770b2a4532',
        username: 'janesmith',
        name: 'Jane Smith',
      });

      const resolved = waitForApi(page, '/api/featureflags/resolved*', 'GET');
      await page.goto('featureflags?userId=5e15a05d-c583-45a0-84fa-1e770b2a4532');

      expect((await resolved).url()).toContain('userId=5e15a05d-c583-45a0-84fa-1e770b2a4532');
      await expect(new FeatureFlagsPage(page).getUserPickerInput()).toHaveValue('janesmith');
    });

    test('toggling a flag while viewing another user targets that user, and the reload stays on them', async ({
      page,
    }) => {
      await mockApi(page, '/api/featureflags/resolved*', 'resolved-features', { method: 'GET' });
      await mockApi(page, '/api/user/5e15a05d-c583-45a0-84fa-1e770b2a4532*', {
        id: '5e15a05d-c583-45a0-84fa-1e770b2a4532',
        username: 'janesmith',
        name: 'Jane Smith',
      });
      await mockApi(page, '/api/featureflag', 'feature-flag-created', { method: 'POST' });

      const flags = new FeatureFlagsPage(page);
      await page.goto('featureflags?userId=5e15a05d-c583-45a0-84fa-1e770b2a4532');

      const create = waitForApi(page, '/api/featureflag', 'POST');
      const reload = waitForApi(page, '/api/featureflags/resolved*userId=*', 'GET');
      await flags.getToggle('project-infographic').click();

      // The write must target the user shown in the URL, not the signed-in admin.
      expect((await create).postDataJSON()).toMatchObject({
        userId: '5e15a05d-c583-45a0-84fa-1e770b2a4532',
        feature: 'project-infographic',
        enabled: true,
      });

      // The reload the effect triggers afterwards must keep targeting that same user.
      expect((await reload).url()).toContain('userId=5e15a05d-c583-45a0-84fa-1e770b2a4532');
      await expect(page).toHaveURL(/userId=5e15a05d-c583-45a0-84fa-1e770b2a4532/);
    });
  });
});
