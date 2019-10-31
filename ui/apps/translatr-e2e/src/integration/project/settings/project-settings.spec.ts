import { ProjectSettingsPage } from '../../../support/project/project-settings-page.po';

describe('Project Settings', () => {
  let page: ProjectSettingsPage;

  beforeEach(() => {
    page = new ProjectSettingsPage('johndoe', 'p1');

    cy.clearCookies();
    cy.server();

    cy.route('/api/me', 'fixture:me');
    cy.route('/api/johndoe/p1', 'fixture:johndoe/p1');
    cy.route('/api/project/*/locales*', 'fixture:johndoe/p1/locales');
    cy.route('/api/project/*/keys*', 'fixture:johndoe/p1/keys');
    cy.route('/api/project/*/messages*', 'fixture:johndoe/p1/messages');
    cy.route('/api/activities/aggregated*',
      'fixture:johndoe/p1/activities-aggregated');
  });

  it('should have page name Project Settings', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getPageName()
      .should('have.text', 'p1');
  });

  it('should have name and description set', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getNameField()
      .should('have.value', 'p1');
    page.getDescriptionField()
      .should('have.value', 'p1d');
  });
});
