import { UsersPage } from '../support/users-page.po';

describe('Admin Auth Guard', () => {
  beforeEach(() => {
    cy.clearCookies();
  });

  it('should redirect a non-admin user to /forbidden', () => {
    // given
    cy.intercept('/api/me*', { fixture: 'me-non-admin' });

    // when
    cy.visit('/users');

    // then
    cy.url().should('contain', '/forbidden');
  });

  it('should let an admin user through to a guarded route', () => {
    // given
    cy.intercept('/api/me*', { fixture: 'me' });
    cy.intercept('/api/user*', { fixture: 'users' });
    cy.intercept('/api/project*', { fixture: 'projects' });
    cy.intercept('/api/activities*', { fixture: 'activities' });

    // when
    new UsersPage().navigateTo();

    // then
    cy.url().should('match', /\/users$/);
    cy.get('h1.page').should('have.text', 'Users');
  });
});
