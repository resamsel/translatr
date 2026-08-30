import { UsersPage } from '../support/users-page.po';

describe('Admin Users', () => {
  let page: UsersPage;

  beforeEach(() => {
    page = new UsersPage();

    cy.clearCookies();

    cy.intercept('/api/me*', { fixture: 'me' });
    cy.intercept('/api/user*', { fixture: 'users' });
    cy.intercept('/api/user/*', { fixture: 'user' });
    cy.intercept('/api/project*', { fixture: 'projects' });
    cy.intercept('/api/activities*', { fixture: 'activities' });
  });

  it('should list the users returned by the API', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getRows().should('have.length', 3);
    page.getRow('Jane Smith').should('contain.text', 'janesmith');
  });

  it('should push the search term into the users request', () => {
    // given
    cy.intercept('/api/user*search=*', { fixture: 'users' }).as('search');

    // when
    page.navigateTo();
    page.getSearchField().type('jane');
    cy.get('mat-option').first().click();

    // then
    cy.wait('@search').its('request.url').should('contain', 'search=jane');
  });

  it('should navigate to the user detail page on row click', () => {
    // given

    // when
    page.navigateTo();
    page.getRows().first().find('a.link').click();

    // then
    cy.url().should('match', /\/users\/[0-9a-f-]+$/);
    page.getPageName().should('have.text', 'Jane Smith');
  });

  it('should persist a role change from the user edit dialog', () => {
    // given
    cy.intercept('PUT', '/api/user*', { fixture: 'user-updated' }).as('update');

    // when
    page.navigateTo();
    page.getEditButton('Jane Smith').click();
    // given: the row's role prefills as Admin before we change it
    page.getDialog().find('mat-select[formControlName="role"]').should('contain.text', 'Admin');
    page.getDialog().find('mat-select[formControlName="role"]').click();
    cy.get('mat-option').contains('User').click();
    page.getDialog().find('button[transloco="button.save"]').click();

    // then
    cy.wait('@update').its('request.body').should('deep.include', {
      id: '5e15a05d-c583-45a0-84fa-1e770b2a4532',
      role: 'User'
    });
  });
});
