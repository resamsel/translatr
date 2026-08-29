import { UsersPage } from '../../support/users-page.po';

describe('Users', () => {
  let page: UsersPage;

  beforeEach(() => {
    page = new UsersPage();

    cy.clearCookies();

    cy.intercept('/api/me?fetch=features', { fixture: 'me' });
    cy.intercept('/api/user*', { fixture: 'users' });
  });

  it('should list users', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getRows().should('have.length', 3);
  });

  it('should filter on search input', () => {
    // given
    cy.intercept('/api/user*search=*', { fixture: 'users-search' }).as('search');

    // when
    page.navigateTo();
    page.getSearchField().type('jane');
    cy.get('mat-option').first().click();

    // then
    cy.wait('@search').its('request.url').should('contain', 'search=jane');
    page.getRows().should('have.length', 1);
  });

  it('should load more users', () => {
    // given
    cy.intercept('/api/user*limit=16*', { fixture: 'users-page2' }).as('more');

    // when
    page.navigateTo();
    page.getRows().should('have.length', 3);
    page.getLoadMoreButton().click();

    // then
    cy.wait('@more').its('request.url').should('contain', 'limit=16');
    page.getRows().should('have.length', 3);
    page.getRows().first().should('contain', 'Ronny Lee');
  });

  it('should show no user rows when there are no users', () => {
    // given
    cy.intercept('/api/user*', { fixture: 'users-empty' });

    // when
    page.navigateTo();

    // then
    cy.get('app-user-list mat-nav-list').should('exist');
    page.getRows().should('have.length', 0);
  });

  it('should navigate to a user page on row click', () => {
    // given
    cy.intercept('/api/johndoe', { fixture: 'johndoe' });
    cy.intercept('/api/projects*', { fixture: 'johndoe/projects' });
    cy.intercept('/api/activities*', { fixture: 'johndoe/activities' });

    // when
    page.navigateTo();
    page.getRows().first().click();

    // then
    cy.url().should('match', /\/johndoe$/);
  });
});
