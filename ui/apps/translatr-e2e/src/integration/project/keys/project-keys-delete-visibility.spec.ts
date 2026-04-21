import { ProjectKeysPage } from '../../../support/project/project-keys-page.po';

describe('Project Keys Delete Key Visibility', () => {
  let page: ProjectKeysPage;

  beforeEach(() => {
    page = new ProjectKeysPage('johndoe', 'p1');

    cy.clearCookies();

    cy.intercept('/api/project/*/locales*', { fixture: 'johndoe/p1/locales' });
    cy.intercept('/api/project/*/keys*', { fixture: 'johndoe/p1/keys' });
    cy.intercept('/api/project/*/messages*', { fixture: 'johndoe/p1/messages' });
    cy.intercept('/api/project/*/members*', { fixture: 'johndoe/p1/members' });
    cy.intercept('/api/project/*/activities*', { fixture: 'johndoe/p1/activities' });
    cy.intercept('/api/activities/aggregated*', { fixture: 'johndoe/p1/activities-aggregated' });
  });

  it('should show key delete button for member role Developer', () => {
    // given
    cy.intercept('/api/me?fetch=features', { fixture: 'janesmith' });
    cy.intercept('/api/johndoe/p1*', { fixture: 'johndoe/p1-developer' });

    // when
    page.navigateTo();

    // then
    page
      .getKeyList()
      .find('confirm-button.delete')
      .should('have.length', 2);
  });

  it('should not show key delete button for member role Translator', () => {
    // given
    cy.intercept('/api/me?fetch=features', { fixture: 'sophiaoreilly' });
    cy.intercept('/api/johndoe/p1*', { fixture: 'johndoe/p1-translator' });

    // when
    page.navigateTo();

    // then
    page
      .getKeyList()
      .find('confirm-button.delete')
      .should('have.length', 0);
  });

  it('should show key delete button for member role Manager', () => {
    // given
    cy.intercept('/api/me?fetch=features', { fixture: 'ronnylee' });
    cy.intercept('/api/johndoe/p1*', { fixture: 'johndoe/p1-manager' });

    // when
    page.navigateTo();

    // then
    page
      .getKeyList()
      .find('confirm-button.delete')
      .should('have.length', 2);
  });

  it('should show key delete button for user role Admin', () => {
    // given
    cy.intercept('/api/me?fetch=features', { fixture: 'anneearth' });
    cy.intercept('/api/johndoe/p1*', { fixture: 'johndoe/p1' });

    // when
    page.navigateTo();

    // then
    page
      .getKeyList()
      .find('confirm-button.delete')
      .should('have.length', 2);
  });
});
