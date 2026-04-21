import { ProjectLocalesPage } from '../../../support/project/project-locales-page.po';

describe('Project Locales Delete Locale', () => {
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

  it('should show locale delete button', () => {
    // given

    // when
    page.navigateTo();

    // then
    page
      .getLocaleList()
      .find('confirm-button.delete')
      .should('be.visible');
  });

  it('should show locale delete menu on clicking delete button', () => {
    // given

    // when
    page.navigateTo();
    page
      .getLocaleList()
      .find('confirm-button.delete')
      .first()
      .click();

    // then
    cy.get('.mat-mdc-menu-panel button.confirm').should('have.text', 'Remove');
  });

  it('should delete locale clicking delete button', () => {
    // given
    cy.intercept('DELETE', '/api/locale/*', { fixture: 'johndoe/p1/locales/default' });

    // when
    page.navigateTo();
    page
      .getLocaleList()
      .find('confirm-button.delete')
      .first()
      .click();
    cy.get('.mat-mdc-menu-panel button.confirm').click();

    // then
    page
      .getLocaleList()
      .find('a[mat-list-item]')
      .should('have.length', 1);
  });
});
