import { UserSettingsPage } from '../../../support/user/user-settings-page.po';

describe('User Settings', () => {
  let page: UserSettingsPage;

  beforeEach(() => {
    page = new UserSettingsPage('johndoe');

    cy.clearCookies();

    cy.intercept('/api/me?fetch=features', { fixture: 'me' });
    cy.intercept('/api/johndoe', { fixture: 'johndoe' });
    cy.intercept('/api/projects*', { fixture: 'johndoe/projects' });
    cy.intercept('/api/activities*', { fixture: 'johndoe/activities' });
  });

  it('should show user settings', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getPageName().should('have.text', 'John Doe');
  });

  it('should have name and username set', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getNameField().should('have.value', 'John Doe');
    page.getUsernameField().should('have.value', 'johndoe');
  });
});
