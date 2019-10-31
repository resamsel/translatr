import { ProjectKeysPage } from '../../../support/project/project-keys-page.po';

describe('Project Keys Add Key', () => {
  let page: ProjectKeysPage;

  beforeEach(() => {
    page = new ProjectKeysPage('johndoe', 'p1');

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

  it('should show key add button', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getFloatingActionButton()
      .should('be.visible');
  });

  it('should show key add dialog on clicking add button', () => {
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
      .should('have.text', 'Add Key');
  });

  it('should hide key add dialog on clicking cancel button', () => {
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

  it('should show error for name when using invalid characters', () => {
    // given

    // when
    page.navigateTo();
    page.getFloatingActionButton().click();
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
