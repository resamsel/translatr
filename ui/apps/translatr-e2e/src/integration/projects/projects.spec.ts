import { ProjectsPage } from '../../support/projects-page.po';

describe('Projects', () => {
  let page: ProjectsPage;

  beforeEach(() => {
    page = new ProjectsPage();

    cy.clearCookies();

    cy.intercept('/api/me?fetch=features', { fixture: 'me' });
    cy.intercept('/api/project*', { fixture: 'projects' });
  });

  it('should list projects', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getCards().should('have.length', 3);
  });

  it('should filter on search input', () => {
    // given
    cy.intercept('/api/project*search=*', { fixture: 'projects-search' }).as('s');

    // when
    page.navigateTo();
    page.getSearchField().type('p1');
    cy.get('mat-option').first().click();

    // then
    cy.wait('@s').its('request.url').should('contain', 'search=p1');
    page.getCards().should('have.length', 1);
  });

  it('should create a project from the floating action button and redirect', () => {
    // given
    cy.intercept('POST', '/api/project', { fixture: 'project-created' }).as('create');
    cy.intercept('/api/johndoe/newproj*', { fixture: 'project-created' });
    cy.intercept('/api/project/*/locales*', { body: { list: [], offset: 0, limit: 50, total: 0 } });
    cy.intercept('/api/project/*/keys*', { body: { list: [], offset: 0, limit: 50, total: 0 } });
    cy.intercept('/api/project/*/members*', { body: { list: [], offset: 0, limit: 50, total: 0 } });
    cy.intercept('/api/activities*', { body: { list: [], offset: 0, limit: 20, total: 0 } });
    cy.intercept('/api/activities/aggregated*', { fixture: 'activities-aggregated' });

    // when
    page.navigateTo();
    page.getFloatingActionButton().click();
    page.getDialog().find('input').first().clear().type('newproj');
    page.getDialog().find('button[transloco="button.save"]').should('not.be.disabled').click();

    // then
    cy.wait('@create').its('request.body').should('have.property', 'name', 'newproj');
    cy.url().should('include', '/johndoe/newproj');
  });
});
