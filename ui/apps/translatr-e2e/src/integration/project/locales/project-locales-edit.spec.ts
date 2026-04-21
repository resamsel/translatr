import { ProjectLocalesPage } from '../../../support/project/project-locales-page.po';

describe('Project Locales Edit Locale', () => {
  let page: ProjectLocalesPage;

  beforeEach(() => {
    page = new ProjectLocalesPage('johndoe', 'p1');

    cy.clearCookies();

    cy.intercept('/api/me?fetch=features', { fixture: 'me' });
    cy.intercept('/api/johndoe/p1*', { fixture: 'johndoe/p1' });
    cy.intercept('/api/project/*/locales*', { fixture: 'johndoe/p1/locales' });
    cy.intercept('/api/project/*/keys*', { fixture: 'johndoe/p1/keys' });
    cy.intercept('/api/project/*/messages*', { fixture: 'johndoe/p1/messages' });
    cy.intercept('/api/project/*/members*', { fixture: 'johndoe/p1/members' });
    cy.intercept('/api/project/*/activities*', { fixture: 'johndoe/p1/activities' });
    cy.intercept('/api/activities/aggregated*', { fixture: 'johndoe/p1/activities-aggregated' });
  });

  it('should show locale edit button', () => {
    // given

    // when
    page.navigateTo();

    // then
    page
      .getLocaleList()
      .find('button.edit')
      .should('be.visible');
  });

  it('should show locale edit dialog on clicking edit button', () => {
    // given

    // when
    page.navigateTo();
    page
      .getLocaleList()
      .find('button.edit')
      .first()
      .click();

    // then
    page.getDialog().should('be.visible');
    page
      .getDialog()
      .find('[mat-dialog-title]')
      .should('have.text', 'Edit Language');
    page
      .getDialog()
      .find('mat-form-field.name input')
      .should('have.value', 'de');
  });

  it('should hide locale edit dialog on clicking cancel button', () => {
    // given

    // when
    page.navigateTo();
    page
      .getLocaleList()
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
