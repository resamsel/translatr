import { ProjectMembersPage } from '../../../support/project/project-members-page.po';

describe('Project Members Modify Member Visibility', () => {
  let page: ProjectMembersPage;

  beforeEach(() => {
    page = new ProjectMembersPage('johndoe', 'p1');

    cy.clearCookies();

    cy.intercept('/api/project/*/locales*', { fixture: 'johndoe/p1/locales' });
    cy.intercept('/api/project/*/keys*', { fixture: 'johndoe/p1/keys' });
    cy.intercept('/api/project/*/messages*', { fixture: 'johndoe/p1/messages' });
    cy.intercept('/api/project/*/members*', { fixture: 'johndoe/p1/members' });
    cy.intercept('/api/project/*/activities*', { fixture: 'johndoe/p1/activities' });
    cy.intercept('/api/activities/aggregated*', { fixture: 'johndoe/p1/activities-aggregated' });
  });

  it('should not show edit/delete controls for member role Developer', () => {
    // given
    cy.intercept('/api/me?fetch=features', { fixture: 'janesmith' });
    cy.intercept('/api/johndoe/p1*', { fixture: 'johndoe/p1-developer' });

    // when
    page.navigateTo();

    // then
    page.getMemberRows().should('have.length', 4);
    page.getMemberRow('Ronny Lee').find('button.edit').should('not.exist');
    page.getMemberRow('Ronny Lee').find('confirm-button.delete').should('not.exist');
  });

  it('should not show edit/delete controls for member role Translator', () => {
    // given
    cy.intercept('/api/me?fetch=features', { fixture: 'sophiaoreilly' });
    cy.intercept('/api/johndoe/p1*', { fixture: 'johndoe/p1-translator' });

    // when
    page.navigateTo();

    // then
    page.getMemberRows().should('have.length', 4);
    page.getMemberRow('Ronny Lee').find('button.edit').should('not.exist');
    page.getMemberRow('Ronny Lee').find('confirm-button.delete').should('not.exist');
  });

  it('should show edit/delete controls for member role Manager', () => {
    // given
    cy.intercept('/api/me?fetch=features', { fixture: 'ronnylee' });
    cy.intercept('/api/johndoe/p1*', { fixture: 'johndoe/p1-manager' });

    // when
    page.navigateTo();

    // then
    page.getMemberRow('Jane Smith').find('button.edit').should('exist');
    page.getMemberRow('Jane Smith').find('confirm-button.delete').should('exist');
  });

  it('should show edit/delete controls for user role Admin', () => {
    // given
    cy.intercept('/api/me?fetch=features', { fixture: 'anneearth' });
    cy.intercept('/api/johndoe/p1*', { fixture: 'johndoe/p1' });

    // when
    page.navigateTo();

    // then
    page.getMemberRow('Jane Smith').find('button.edit').should('exist');
    page.getMemberRow('Jane Smith').find('confirm-button.delete').should('exist');
  });
});
