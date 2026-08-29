import { LoginPage } from '../../support/auth/login-page.po';

const authClients = [
  { key: 'keycloak', url: '/login/keycloak' },
  { key: 'google', url: '/login/google' }
];

describe('Login', () => {
  let page: LoginPage;

  beforeEach(() => {
    page = new LoginPage();

    cy.clearCookies();

    cy.intercept('/api/me*', { statusCode: 401, body: {} });
    cy.intercept('/api/authclients', { body: authClients });
  });

  it('should render the login page with provider links', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getPageName().should('have.text', 'Sign in');
    page.getProviderLinks().should('have.length', 2);
    cy.get('a.client-keycloak').should('exist');
  });

  it('should redirect an unauthenticated visit to /dashboard to /login', () => {
    // given

    // when
    cy.visit('/dashboard');

    // then
    cy.url().should('contain', '/login');
  });
});
