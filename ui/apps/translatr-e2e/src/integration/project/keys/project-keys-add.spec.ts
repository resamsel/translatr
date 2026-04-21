import { ProjectKeysPage } from '../../../support/project/project-keys-page.po';

describe('Project Keys Add Key', () => {
  let page: ProjectKeysPage;

  beforeEach(() => {
    page = new ProjectKeysPage('johndoe', 'p1');

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

  it('should show key add button', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getFloatingActionButton().should('be.visible');
  });

  it('should show key add dialog on clicking add button', () => {
    // given

    // when
    page.navigateTo();
    page.getFloatingActionButton().click();

    // then
    page.getDialog().should('be.visible');
    page
      .getDialog()
      .find('mat-form-field.name input')
      .should('have.value', '');
    page
      .getDialog()
      .find('[mat-dialog-title]')
      .should('have.text', 'Add Key');
  });

  it('should hide key add dialog on clicking cancel button', () => {
    // given

    // when
    page.navigateTo();
    page.getFloatingActionButton().click();
    page
      .getDialog()
      .find('button.cancel')
      .click();

    // then
    page.getDialog().should('have.length', 0);
  });
});
