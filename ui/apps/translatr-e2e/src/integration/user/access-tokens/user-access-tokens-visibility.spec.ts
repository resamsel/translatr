import { UserAccessTokensPage } from '../../../support/user/user-access-tokens-page.po';

describe('User Access Tokens Visibility', () => {
  beforeEach(() => {
    cy.clearCookies();

    cy.intercept('/api/me?fetch=features', { fixture: 'janesmith' });
    cy.intercept('/api/johndoe', { fixture: 'johndoe' });
    cy.intercept('/api/projects*', { fixture: 'johndoe/projects' });
    cy.intercept('/api/activities*', { fixture: 'johndoe/activities' });
    cy.intercept('/api/accesstokens*', { fixture: 'johndoe/access-tokens' });
  });

  it('should block another user from johndoe access-tokens (MyselfGuard)', () => {
    // given
    const page = new UserAccessTokensPage('johndoe');

    // when
    page.navigateTo();

    // then — MyselfGuard cancels the navigation, the screen never renders
    cy.get('app-user-access-tokens').should('not.exist');
    cy.get('.floating-action-btn').should('not.exist');
  });
});
