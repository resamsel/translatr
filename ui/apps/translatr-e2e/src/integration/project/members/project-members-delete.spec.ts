import { ProjectMembersPage } from '../../../support/project/project-members-page.po';

describe('Project Members Delete Member', () => {
  let page: ProjectMembersPage;

  beforeEach(() => {
    page = new ProjectMembersPage('johndoe', 'p1');

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

  it('should show the delete button on a non-owner row', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getDeleteButton('Ronny Lee').should('exist');
  });

  it('should not show a delete button on the sole Owner row', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getMemberRow('John Doe').find('confirm-button.delete').should('not.exist');
  });

  it('should show the confirmation menu on clicking delete', () => {
    // given

    // when
    page.navigateTo();
    page.getDeleteButton('Ronny Lee').click();

    // then
    cy.get('.mat-mdc-menu-panel button.confirm').should('have.text', 'Remove');
  });

  it('should remove the row after confirming delete', () => {
    // given
    page.navigateTo();
    page.getMemberRows().should('have.length', 4);
    cy.intercept('DELETE', '/api/member/*', { fixture: 'johndoe/p1/member-deleted' }).as(
      'deleteMember'
    );
    cy.intercept('/api/project/*/members*', { fixture: 'johndoe/p1/members-minus-ronny' });

    // when
    page.getDeleteButton('Ronny Lee').click();
    cy.get('.mat-mdc-menu-panel button.confirm').click();

    // then
    cy.wait('@deleteMember');
    page.getMemberRow('Ronny Lee').should('not.exist');
    page.getMemberRows().should('have.length', 3);
  });
});
