import { ProjectLocalesPage } from '../../../support/project/project-locales-page.po';

describe('Project Locales Add Locale Visibility', () => {
  let page: ProjectLocalesPage;

  beforeEach(() => {
    page = new ProjectLocalesPage('johndoe', 'p1');

    cy.clearCookies();
    cy.server();

    cy.route('/api/project/*/locales*', 'fixture:johndoe/p1/locales');
    cy.route('/api/project/*/keys*', 'fixture:johndoe/p1/keys');
    cy.route('/api/project/*/messages*', 'fixture:johndoe/p1/messages');
    cy.route('/api/project/*/members*', 'fixture:johndoe/p1/members');
    cy.route('/api/project/*/activities*', 'fixture:johndoe/p1/activities');
    cy.route('/api/activities/aggregated*',
      'fixture:johndoe/p1/activities-aggregated');
  });

  it('should not show locale add button for member role Developer', () => {
    // given
    cy.route('/api/me?fetch=features', 'fixture:janesmith');
    cy.route('/api/johndoe/p1*', 'fixture:johndoe/p1-developer');

    // when
    page.navigateTo();

    // then
    page.getFloatingActionButton().should('have.length', 0);
  });

  it('should show locale add button for member role Translator', () => {
    // given
    cy.route('/api/me?fetch=features', 'fixture:sophiaoreilly');
    cy.route('/api/johndoe/p1*', 'fixture:johndoe/p1-translator');

    // when
    page.navigateTo();

    // then
    page.getFloatingActionButton().should('have.length', 1);
  });

  it('should show locale add button for member role Manager', () => {
    // given
    cy.route('/api/me?fetch=features', 'fixture:ronnylee');
    cy.route('/api/johndoe/p1*', 'fixture:johndoe/p1-manager');

    // when
    page.navigateTo();

    // then
    page.getFloatingActionButton().should('have.length', 1);
  });

  it('should show locale add button for user role Admin', () => {
    // given
    cy.route('/api/me?fetch=features', 'fixture:anneearth');
    cy.route('/api/johndoe/p1*', 'fixture:johndoe/p1');

    // when
    page.navigateTo();

    // then
    page.getFloatingActionButton().should('have.length', 1);
  });
});
