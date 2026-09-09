import { test, expect } from '../../support/test';
import { mockApi } from '../../support/mock-api';

test.describe('Not Found', () => {
  test('should show the not-found page when the project API answers 404', async ({ page }) => {
    await mockApi(
      page,
      '/api/johndoe/nope*',
      { error: { type: 'NotFoundException', entity: 'Project' } },
      { status: 404 },
    );

    await page.goto('johndoe/nope');

    // the AuthInterceptor routes 404 to the not-found page (skipLocationChange)
    const header = page.locator('.error-header');
    await expect(header).toBeVisible();
    await expect(header).toContainText('Page not found');
  });

  test('should land on /not-found for an unknown top-level route', async ({ page }) => {
    await mockApi(page, '/api/this-route-does-not-exist*', null, { status: 200 });

    await page.goto('this-route-does-not-exist');

    // UserGuard cannot resolve the username and redirects to /not-found
    await expect(page).toHaveURL(/\/not-found/);
    const header = page.locator('.error-header');
    await expect(header).toBeVisible();
    await expect(header).toContainText('Page not found');
  });
});
