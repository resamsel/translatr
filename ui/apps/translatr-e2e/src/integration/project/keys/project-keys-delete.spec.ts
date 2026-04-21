import { ProjectKeysPage } from '../../../support/project/project-keys-page.po';

describe('Project Keys Delete Key', () => {
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

  it('should show key delete button', () => {
    // given

    // when
    page.navigateTo();

    // then
    page
      .getKeyList()
      .find('confirm-button.delete')
      .should('be.visible');
  });

  it('should show key delete menu on clicking delete button', () => {
    // given

    // when
    page.navigateTo();
    page
      .getKeyList()
      .find('confirm-button.delete')
      .first()
      .click();

    // then
    cy.get('.mat-mdc-menu-panel button.confirm').should('have.text', 'Remove');
  });

  it('should delete key clicking delete button', () => {
    // given
    cy.intercept('DELETE', '/api/key/*', { fixture: 'johndoe/p1/keys/k1' });

    // when
    page.navigateTo();
    page
      .getKeyList()
      .find('confirm-button.delete')
      .first()
      .click();
    cy.get('.mat-mdc-menu-panel button.confirm').click();

    // then
    page
      .getKeyList()
      .find('a[mat-list-item]')
      .should('have.length', 1);
  });
});
