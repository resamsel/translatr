import { ProjectLocalesPage } from '../../../support/project/project-locales-page.po';

describe('Project Locales Edit Locale', () => {
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

  it('should show locale edit button', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getLocaleList()
      .find('button.edit')
      .should('be.visible');
  });

  it('should show locale edit dialog on clicking edit button', () => {
    // given

    // when
    page.navigateTo();
    page.getLocaleList()
      .find('button.edit')
      .first()
      .click();

    // then
    page.getDialog().should('be.visible');
    page.getDialog()
      .find('.mat-dialog-title')
      .should('have.text', 'Edit Locale');
    page.getDialog()
      .find('.mat-form-field.name input')
      .should('have.value', 'de');
  });

  it('should hide locale edit dialog on clicking cancel button', () => {
    // given

    // when
    page.navigateTo();
    page.getLocaleList()
      .find('button.edit')
      .first()
      .click();
    page.getDialog()
      .find('button.cancel')
      .click();

    // then
    page.getDialog().should('have.length', 0);
  });
});
