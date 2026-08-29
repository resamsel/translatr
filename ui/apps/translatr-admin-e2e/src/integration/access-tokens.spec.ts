import { AccessTokensPage } from '../support/access-tokens-page.po';

describe('Admin Access Tokens', () => {
  let page: AccessTokensPage;

  beforeEach(() => {
    page = new AccessTokensPage();

    cy.clearCookies();

    cy.intercept('/api/me*', { fixture: 'me' });
    cy.intercept('/api/accesstokens*', { fixture: 'access-tokens' });
  });

  it('should list the access tokens returned by the API', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getPageName().should('have.text', 'Access Tokens');
    page.getRows().should('have.length', 2);
    page.getRows().first().should('contain.text', 'CI token');
  });

  it('should persist an edit from the access token dialog', () => {
    // given
    cy.intercept('PUT', '/api/accesstoken', { fixture: 'access-token-updated' }).as('update');

    // when
    page.navigateTo();
    page.getEditButton('CI token').click();
    page.getDialog().find('input').first().clear().type('renamed');
    page.getDialog().find('button[transloco="button.save"]').click();

    // then
    cy.wait('@update').its('request.body').should('deep.include', {
      id: 1001,
      name: 'renamed'
    });
  });
});
