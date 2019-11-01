import { ProjectKeysPage } from '../../../support/project/project-keys-page.po';

describe('Project Keys Edit Key Visibility', () => {
  let page: ProjectKeysPage;

  beforeEach(() => {
    page = new ProjectKeysPage('johndoe', 'p1');

    cy.clearCookies();
    cy.server();

    cy.route('/api/johndoe/p1', 'fixture:johndoe/p1');
    cy.route('/api/project/*/locales*', 'fixture:johndoe/p1/locales');
    cy.route('/api/project/*/keys*', 'fixture:johndoe/p1/keys');
    cy.route('/api/project/*/messages*', 'fixture:johndoe/p1/messages');
    cy.route('/api/activities/aggregated*',
      'fixture:johndoe/p1/activities-aggregated');
  });

  it('should show key delete button for member role Developer', () => {
    // given
    cy.route('/api/me', 'fixture:janesmith');

    // when
    page.navigateTo();

    // then
    page.getKeyList()
      .find('button.edit')
      .should('have.length', 2);
  });

  it('should not show key delete button for member role Translator', () => {
    // given
    cy.route('/api/me', 'fixture:sophiaoreilly');

    // when
    page.navigateTo();

    // then
    page.getKeyList()
      .find('button.edit')
      .should('have.length', 0);
  });

  it('should show key delete button for member role Manager', () => {
    // given
    cy.route('/api/me', 'fixture:ronnylee');

    // when
    page.navigateTo();

    // then
    page.getKeyList()
      .find('button.edit')
      .should('have.length', 2);
  });

  it('should show key delete button for user role Admin', () => {
    // given
    cy.route('/api/me', 'fixture:anneearth');

    // when
    page.navigateTo();

    // then
    page.getKeyList()
      .find('button.edit')
      .should('have.length', 2);
  });
});