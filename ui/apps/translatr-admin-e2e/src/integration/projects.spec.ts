import { ProjectsPage } from '../support/projects-page.po';

describe('Admin Projects', () => {
  let page: ProjectsPage;

  beforeEach(() => {
    page = new ProjectsPage();

    cy.clearCookies();

    cy.intercept('/api/me*', { fixture: 'me' });
    cy.intercept('/api/project*', { fixture: 'projects' });
  });

  it('should list the projects returned by the API', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getPageName().should('have.text', 'Projects');
    page.getRows().should('have.length', 3);
    page.getRows().first().should('contain.text', 'alpha');
  });

  it('should push the search term into the projects request', () => {
    // given
    cy.intercept('/api/project*search=*', { fixture: 'projects' }).as('search');

    // when
    page.navigateTo();
    page.getSearchField().type('beta');
    cy.get('mat-option').first().click();

    // then
    cy.wait('@search').its('request.url').should('contain', 'search=beta');
  });
});
