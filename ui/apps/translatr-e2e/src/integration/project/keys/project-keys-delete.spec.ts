import { ProjectKeysPage } from '../../../support/project/project-keys-page.po';

describe('Project Keys Delete Key', () => {
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
    cy.get('.mat-menu-panel button.confirm').should('have.text', 'Remove');
  });

  it('should delete key clicking delete button', () => {
    // given
    cy.route('DELETE', '/api/key/*', 'fixture:johndoe/p1/keys/k1');

    // when
    page.navigateTo();
    page
      .getKeyList()
      .find('confirm-button.delete')
      .first()
      .click();
    cy.get('.mat-menu-panel button.confirm').click();

    // then
    page
      .getKeyList()
      .find('.mat-list-item')
      .should('have.length', 1);
  });
});
