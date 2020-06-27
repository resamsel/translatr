import { ProjectKeysPage } from '../../../support/project/project-keys-page.po';

describe('Project Keys Edit Key', () => {
  let page: ProjectKeysPage;

  beforeEach(() => {
    page = new ProjectKeysPage('johndoe', 'p1');

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

  it('should show key edit button', () => {
    // given

    // when
    page.navigateTo();

    // then
    page
      .getKeyList()
      .find('button.edit')
      .should('be.visible');
  });

  it('should show key edit dialog on clicking edit button', () => {
    // given

    // when
    page.navigateTo();
    page
      .getKeyList()
      .find('button.edit')
      .first()
      .click();

    // then
    page.getDialog().should('be.visible');
    page
      .getDialog()
      .find('.mat-dialog-title')
      .should('have.text', 'Edit Key');
    page
      .getDialog()
      .find('.mat-form-field.name input')
      .should('have.value', 'k1');
  });

  it('should hide key edit dialog on clicking cancel button', () => {
    // given

    // when
    page.navigateTo();
    page
      .getKeyList()
      .find('button.edit')
      .first()
      .click();
    page
      .getDialog()
      .find('button.cancel')
      .click();

    // then
    page.getDialog().should('have.length', 0);
  });
});
