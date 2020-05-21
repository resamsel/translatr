import { ProjectLocalesPage } from '../../../support/project/project-locales-page.po';

describe('Project Locales Add Locale', () => {
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
    cy.route('/api/activities/aggregated*',
      'fixture:johndoe/p1/activities-aggregated');
  });

  it('should show locale add button', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getFloatingActionButton()
      .should('be.visible');
  });

  it('should show locale add dialog on clicking add button', () => {
    // given

    // when
    page.navigateTo();
    page.getFloatingActionButton().click();

    // then
    page.getDialog().should('be.visible');
    page.getDialog()
      .find('.mat-form-field.name input')
      .should('have.value', '');
    page.getDialog()
      .find('.mat-dialog-title')
      .should('have.text', 'Add Language');
  });

  it('should hide locale add dialog on clicking cancel button', () => {
    // given

    // when
    page.navigateTo();
    page.getFloatingActionButton().click();
    page.getDialog()
      .find('button.cancel')
      .click();

    // then
    page.getDialog().should('have.length', 0);
  });
});
