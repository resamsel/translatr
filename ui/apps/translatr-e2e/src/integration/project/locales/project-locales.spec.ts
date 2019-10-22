import { ProjectLocalesPage } from '../../../support/project/project-locales-page.po';

describe('Translatr Project Locales', () => {
  let page: ProjectLocalesPage;

  beforeEach(() => {
    page = new ProjectLocalesPage('johndoe', 'p1');

    cy.clearCookies();
    cy.server();

    cy.route('/api/me', 'fixture:me');
    cy.route('/api/johndoe/p1', 'fixture:project/johndoe-p1');
    cy.route('/api/project/*/locales*', 'fixture:project/johndoe-p1-locales');
    cy.route('/api/project/*/keys*', 'fixture:project/johndoe-p1-keys');
    cy.route('/api/project/*/messages*', 'fixture:project/johndoe-p1-messages');
    cy.route('/api/activities/aggregated*',
      'fixture:project/johndoe-p1-activities-aggregated');
  });

  it('should have page name Project Locales', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getPageName()
      .should('have.text', 'p1');
  });
});
