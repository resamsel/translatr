import { test, expect } from '../../support/test';
import { mockApi, mockUnauthenticated, waitForApi } from '../../support/mock-api';
import { RegistrationPage } from '../../support/auth/registration-page.po';

test.describe('Registration', () => {
  test.beforeEach(async ({ page }) => {
    await mockUnauthenticated(page);
    await mockApi(page, '/api/profile', null, { status: 200 });
  });

  test('should show the username field', async ({ page }) => {
    const registration = await new RegistrationPage(page).navigateTo();

    await expect(registration.getUsernameField()).toBeVisible();
  });

  test('should keep submit disabled until name and username are filled', async ({ page }) => {
    const registration = await new RegistrationPage(page).navigateTo();

    await expect(registration.getSubmitButton()).toBeDisabled();

    await registration.fillName('Fresh User');
    await registration.fillUsername('freshuser');
    await expect(registration.getSubmitButton()).toBeEnabled();
  });

  // Skipped: UserService.create() has no generated endpoint to delegate to
  // (openapi.yaml's users tag has no create operation) and now errors without
  // issuing a request — see #282 (design.md Decision 8) and the follow-up
  // https://github.com/resamsel/translatr/issues/296. Un-skip once #296 lands.
  test.skip('should show a field error when the username is not unique', async ({ page }) => {
    await mockApi(page, '/api/user', 'johndoe-register-not-unique', {
      method: 'POST',
      status: 400,
    });
    const registration = await new RegistrationPage(page).navigateTo();

    await registration.fillName('John Doe');
    await registration.fillUsername('johndoe');
    const register = waitForApi(page, '/api/user', 'POST');
    await registration.getSubmitButton().click();
    await register;

    await expect(registration.getUsernameError()).toBeVisible();
    await expect(registration.getUsernameError()).toContainText('Username already taken');
  });

  // Skipped: see https://github.com/resamsel/translatr/issues/296.
  test.skip('should redirect to the dashboard on success', async ({ page }) => {
    await mockApi(page, '/api/user', 'johndoe', { method: 'POST' });
    // the session is valid once registration succeeded, so /dashboard's AuthGuard passes
    await mockApi(page, '/api/me*', 'me');
    await mockApi(page, '/api/users?limit=1&fetch=count', 'dashboard/empty/users-limit1');
    await mockApi(page, '/api/projects?*ownerUsername=*', 'dashboard/empty/projects-owner-limit4');
    await mockApi(page, '/api/projects?*memberId=*', 'dashboard/empty/projects-memberId-limit4');
    await mockApi(page, '/api/activities*', 'dashboard/empty/activities-userId-limit4');
    const registration = await new RegistrationPage(page).navigateTo();

    await registration.fillName('Fresh User');
    await registration.fillUsername('freshuser');
    const register = waitForApi(page, '/api/user', 'POST');
    await registration.getSubmitButton().click();
    await register;

    await expect(page).toHaveURL(/\/dashboard/);
  });
});
