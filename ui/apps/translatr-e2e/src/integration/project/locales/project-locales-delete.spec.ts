import { ProjectLocalesPage } from '../../../support/project/project-locales-page.po';

describe('Project Locales Delete Locale', () => {
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

  it('should show locale delete button', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getLocaleList()
      .find('confirm-button.delete')
      .should('be.visible');
  });

  it('should show locale delete menu on clicking delete button', () => {
    // given

    // when
    page.navigateTo();
    page.getLocaleList()
      .find('confirm-button.delete')
      .first()
      .click();

    // then
    cy.get('.mat-menu-panel button.confirm')
      .should('have.text', 'Delete');
  });

  it('should delete locale clicking delete button', () => {
    // given
    cy.route('DELETE', '/api/locale/*', 'fixture:johndoe/p1/locales/default');

    // when
    page.navigateTo();
    page.getLocaleList()
      .find('confirm-button.delete')
      .first()
      .click();
    cy.get('.mat-menu-panel button.confirm')
      .click();

    // then
    page.getLocaleList()
      .find('.mat-list-item')
      .should('have.length', 1);
  });
});
