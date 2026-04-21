import { ProjectKeysPage } from '../../../support/project/project-keys-page.po';

describe('Project Keys Edit Key', () => {
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
      .find('[mat-dialog-title]')
      .should('have.text', 'Edit Key');
    page
      .getDialog()
      .find('mat-form-field.name input')
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
