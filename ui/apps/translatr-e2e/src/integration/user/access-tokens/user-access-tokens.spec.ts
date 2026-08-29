import { UserAccessTokensPage } from '../../../support/user/user-access-tokens-page.po';

describe('User Access Tokens', () => {
  let page: UserAccessTokensPage;

  beforeEach(() => {
    page = new UserAccessTokensPage('johndoe');

    cy.clearCookies();

    cy.intercept('/api/me?fetch=features', { fixture: 'me' });
    cy.intercept('/api/johndoe', { fixture: 'johndoe' });
    cy.intercept('/api/projects*', { fixture: 'johndoe/projects' });
    cy.intercept('/api/activities*', { fixture: 'johndoe/activities' });
    cy.intercept('/api/accesstokens*', { fixture: 'johndoe/access-tokens' });
  });

  it('should list the access tokens', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getRows().should('have.length', 2);
    page.getRows().should('contain.text', 'CI token');
    page.getRows().should('contain.text', 'Local dev');
  });

  it('should show an empty view when there are none', () => {
    // given
    cy.intercept('/api/accesstokens*', { fixture: 'johndoe/access-tokens-empty' });

    // when
    page.navigateTo();

    // then
    page.getEmptyView().should('be.visible');
    page.getRows().should('have.length', 0);
  });

  it('should navigate to the create page from the FAB', () => {
    // given

    // when
    page.navigateTo();
    page.getFloatingActionButton().click();

    // then
    cy.url().should('contain', '/johndoe/access-tokens/create');
  });
});
