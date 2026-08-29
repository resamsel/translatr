import { UserAccessTokensPage } from '../../../support/user/user-access-tokens-page.po';
import { UserAccessTokenPage } from '../../../support/user/user-access-token-page.po';

describe('User Access Token Edit', () => {
  const tokenId = 'a1000000-0000-0000-0000-000000000001';

  beforeEach(() => {
    cy.clearCookies();

    cy.intercept('/api/me?fetch=features', { fixture: 'me' });
    cy.intercept('/api/johndoe', { fixture: 'johndoe' });
    cy.intercept('/api/projects*', { fixture: 'johndoe/projects' });
    cy.intercept('/api/activities*', { fixture: 'johndoe/activities' });
    cy.intercept('/api/accesstokens*', { fixture: 'johndoe/access-tokens' });
    cy.intercept('/api/accesstoken/*', { fixture: 'johndoe/access-token' });
  });

  it('should populate the fields from the token', () => {
    // given
    const page = new UserAccessTokenPage('johndoe', tokenId).navigateTo();

    // when

    // then
    page.getNameField().should('have.value', 'CI token');
  });

  it('should persist on save and confirm with a snackbar', () => {
    // given
    cy.intercept('PUT', '/api/accesstoken', { statusCode: 200, body: {} }).as('updateAccessToken');
    const page = new UserAccessTokenPage('johndoe', tokenId).navigateTo();

    // when
    page.getNameField().type('{selectall}CI token renamed');
    page.getSaveButton().click();

    // then
    cy.wait('@updateAccessToken');
    cy.get('.mat-mdc-snack-bar-label').should('be.visible');
    cy.url().should('contain', '/johndoe/access-tokens');
  });

  it('should delete a token from the list and remove its row', () => {
    // given
    const listPage = new UserAccessTokensPage('johndoe').navigateTo();
    listPage.getRows().should('have.length', 2);
    cy.intercept('DELETE', '/api/accesstoken/*', { fixture: 'johndoe/access-token' }).as(
      'deleteAccessToken'
    );
    cy.intercept('/api/accesstokens*', { fixture: 'johndoe/access-tokens-minus-one' });

    // when
    cy.contains('app-user-access-tokens a[mat-list-item]', 'CI token')
      .find('confirm-button.delete button')
      .click();
    cy.get('.mat-mdc-menu-panel button.confirm').click();

    // then
    cy.wait('@deleteAccessToken');
    listPage.getRows().should('have.length', 1);
    cy.contains('app-user-access-tokens a[mat-list-item]', 'CI token').should('not.exist');
  });
});
