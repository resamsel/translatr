import { ProjectLocalesPage } from '../../../support/project/project-locales-page.po';

describe('Project Locales', () => {
  let page: ProjectLocalesPage;

  beforeEach(() => {
    page = new ProjectLocalesPage('johndoe', 'p1');

    cy.clearCookies();
    cy.server();

    cy.route('/api/me?fetch=features', 'fixture:me');
    cy.route('/api/johndoe/p1*', 'fixture:johndoe/p1');
    cy.route('/api/project/*/locales*', 'fixture:johndoe/p1/locales');
    cy.route('/api/project/*/keys*', 'fixture:johndoe/p1/keys');
    cy.route('/api/project/*/messages*', 'fixture:johndoe/p1/messages');
    cy.route('/api/project/*/members*', 'fixture:johndoe/p1/members');
    cy.route('/api/project/*/activities*', 'fixture:johndoe/p1/activities');
    cy.route('/api/activities/aggregated*', 'fixture:johndoe/p1/activities-aggregated');
  });

  it('should have page name Project Locales', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getPageName().should('have.text', 'johndoe/p1');
  });
});
