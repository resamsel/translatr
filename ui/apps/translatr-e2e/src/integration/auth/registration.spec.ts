import { RegistrationPage } from '../../support/auth/registration-page.po';

describe('Registration', () => {
  let page: RegistrationPage;

  beforeEach(() => {
    page = new RegistrationPage();

    cy.clearCookies();

    cy.intercept('/api/me*', { statusCode: 401, body: {} });
    cy.intercept('/api/profile', { statusCode: 200, body: null });
  });

  it('should show the username field', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getUsernameField().should('be.visible');
  });

  it('should keep submit disabled until name and username are filled', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getSubmitButton().should('be.disabled');

    page.fillName('Fresh User');
    page.fillUsername('freshuser');
    page.getSubmitButton().should('not.be.disabled');
  });

  it('should show a field error when the username is not unique', () => {
    // given
    cy.intercept('POST', '/api/user', {
      statusCode: 400,
      fixture: 'johndoe-register-not-unique'
    }).as('register');

    // when
    page.navigateTo();
    page.fillName('John Doe');
    page.fillUsername('johndoe');
    page.getSubmitButton().click();

    // then
    cy.wait('@register');
    page.getUsernameError().should('be.visible').and('contain.text', 'Username already taken');
  });

  it('should redirect to the dashboard on success', () => {
    // given
    cy.intercept('POST', '/api/user', { fixture: 'johndoe' }).as('register');
    // the session is valid once registration succeeded, so /dashboard's AuthGuard passes
    cy.intercept('/api/me*', { fixture: 'me' });
    cy.intercept('/api/users?limit=1&fetch=count', { fixture: 'dashboard/empty/users-limit1' });
    cy.intercept('/api/projects?owner=*', { fixture: 'dashboard/empty/projects-owner-limit4' });
    cy.intercept('/api/projects?memberId=*', { fixture: 'dashboard/empty/projects-memberId-limit4' });
    cy.intercept('/api/activities*', { fixture: 'dashboard/empty/activities-userId-limit4' });

    // when
    page.navigateTo();
    page.fillName('Fresh User');
    page.fillUsername('freshuser');
    page.getSubmitButton().click();

    // then
    cy.wait('@register');
    cy.url().should('contain', '/dashboard');
  });
});
