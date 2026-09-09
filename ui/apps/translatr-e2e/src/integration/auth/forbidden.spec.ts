import { test, expect } from '../../support/test';
import { mockApi } from '../../support/mock-api';

test.describe('Forbidden', () => {
  test('should show the forbidden page when the project API answers 403', async ({ page }) => {
    await mockApi(
      page,
      '/api/johndoe/p1*',
      { error: { type: 'PermissionException', message: 'forbidden' } },
      { status: 403 },
    );

    await page.goto('johndoe/p1');

    // the AuthInterceptor routes 403 to the forbidden page (skipLocationChange)
    const header = page.locator('.error-header');
    await expect(header).toBeVisible();
    await expect(header).toContainText('Forbidden');
  });
});
