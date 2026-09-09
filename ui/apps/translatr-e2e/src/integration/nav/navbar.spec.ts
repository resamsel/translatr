import { test, expect } from '../../support/test';
import { mockApi, remockApi } from '../../support/mock-api';
import { NavbarPage } from '../../support/nav/navbar.po';

test.describe('Navbar', () => {
  test.beforeEach(async ({ page }) => {
    await mockApi(page, '/api/me?fetch=features', 'me');
    await mockApi(page, '/api/users?limit=1&fetch=count', 'dashboard/users-limit1');
    await mockApi(page, '/api/projects?owner=*', 'dashboard/projects-owner-limit4');
    await mockApi(page, '/api/projects?memberId=*', 'dashboard/projects-memberId-limit4');
    await mockApi(page, '/api/activities*', 'dashboard/activities-userId-limit4');
  });

  test('should switch the active language via the language switcher', async ({ page }) => {
    // given
    await remockApi(page, '/api/me?fetch=features', 'me-language-switcher');

    // when
    const nav = await new NavbarPage(page).navigateTo();
    await expect(nav.getLanguageSwitcher()).toHaveCount(1);
    await nav.getLanguageSwitcherTrigger().click();

    // then
    const items = nav.getMenuPanel().locator('button[mat-menu-item]');
    await expect(items).toHaveCount(2);
    await expect(items.nth(0)).toHaveClass(/\bactive\b/);
    await expect(items.nth(1)).not.toHaveClass(/\bactive\b/);

    // when — switch to the second language
    await items.nth(1).click();
    await nav.getLanguageSwitcherTrigger().click();

    // then — the second language is now active
    const itemsAfter = nav.getMenuPanel().locator('button[mat-menu-item]');
    await expect(itemsAfter.nth(1)).toHaveClass(/\bactive\b/);
    await expect(itemsAfter.nth(0)).not.toHaveClass(/\bactive\b/);
  });

  test('should expose profile and logout in the user menu', async ({ page }) => {
    // given

    // when
    const nav = await new NavbarPage(page).navigateTo();
    await nav.getUserMenuTrigger().click();

    // then
    await expect(nav.getMenuPanel()).toBeVisible();
    await expect(nav.getMenuPanel().locator('[transloco="user.profile"]')).toHaveAttribute(
      'href',
      /\/johndoe$/,
    );
    await expect(nav.getMenuPanel().locator('[transloco="auth.logout"]')).toHaveAttribute(
      'href',
      /\/logout/,
    );
  });
});
