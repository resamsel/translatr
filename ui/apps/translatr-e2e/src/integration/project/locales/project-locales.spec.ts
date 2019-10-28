import { ProjectLocalesPage } from '../../../support/project/project-locales-page.po';

describe('Translatr Project Locales', () => {
  let page: ProjectLocalesPage;

  beforeEach(() => {
    page = new ProjectLocalesPage('johndoe', 'p1');

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

  it('should have page name Project Locales', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getPageName().should('have.text', 'p1');
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

  it('should show locale delete button', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getLocaleList()
      .find('confirm-button.delete')
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
      .should('have.text', 'Add Locale');
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

  it('should show error for name when using invalid characters', () => {
    // given

    // when
    page.navigateTo();
    page.getLocaleList()
      .find('button.edit')
      .first()
      .click();
    page.getDialog()
      .find('.mat-form-field.name input')
      .type('{selectall}de space')
      .blur();

    // then
    page.getDialog()
      .find('.mat-form-field.name mat-error')
      .should('be.visible');
    page.getDialog()
      .find('button.save')
      .should('be.disabled');
  });
});
