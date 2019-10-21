import { ProjectPage } from '../../support/project-page.po';

describe('Translatr Project Settings', () => {
  let page: ProjectPage;

  beforeEach(() => {
    page = new ProjectPage('johndoe', 'p1');

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

  it('should have page name p1', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getPageName()
      .should('have.text', 'p1');
  });
});
