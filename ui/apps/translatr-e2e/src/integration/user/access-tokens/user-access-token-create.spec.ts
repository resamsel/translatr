import { UserAccessTokenPage } from '../../../support/user/user-access-token-page.po';

describe('User Access Token Create', () => {
  beforeEach(() => {
    cy.clearCookies();

    cy.intercept('/api/me?fetch=features', { fixture: 'me' });
    cy.intercept('/api/johndoe', { fixture: 'johndoe' });
    cy.intercept('/api/projects*', { fixture: 'johndoe/projects' });
    cy.intercept('/api/activities*', { fixture: 'johndoe/activities' });
    cy.intercept('/api/accesstokens*', { fixture: 'johndoe/access-tokens' });
  });

  it('should create a token and reveal the secret once', () => {
    // given
    cy.intercept('POST', '/api/accesstoken', { fixture: 'johndoe/access-token-created' }).as(
      'createAccessToken'
    );
    const page = new UserAccessTokenPage('johndoe', 'create').navigateTo();

    // when
    page.getNameField().type('New token', { force: true });
    page.getSaveButton().click();

    // then
    cy.wait('@createAccessToken');
    page.getSecret().should('have.value', 'tk_live_ONE_TIME_SECRET_abcdef123456');
  });

  it('should confirm the save with a snackbar and stay on the form', () => {
    // given
    cy.intercept('POST', '/api/accesstoken', { fixture: 'johndoe/access-token-created' }).as(
      'createAccessToken'
    );
    const page = new UserAccessTokenPage('johndoe', 'create').navigateTo();

    // when
    page.getNameField().type('New token', { force: true });
    page.getSaveButton().click();

    // then
    cy.wait('@createAccessToken');
    cy.get('.mat-mdc-snack-bar-label').should('contain.text', 'has been saved');
    cy.url().should('match', /\/johndoe\/access-tokens\/create$/);
  });
});
