import { ProjectMembersPage } from '../../../support/project/project-members-page.po';

describe('Project Members Edit Member', () => {
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

  it('should show the edit button on a non-owner row', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getEditButton('Jane Smith').should('be.visible');
  });

  it('should open the edit dialog pre-filled', () => {
    // given

    // when
    page.navigateTo();
    page.getEditButton('Jane Smith').click();

    // then
    page.getDialog().should('have.length', 1);
    page
      .getDialogTitle()
      .should('have.attr', 'transloco', 'member.edit.title');
    page
      .getDialog()
      .find('input')
      .first()
      .should('have.value', 'janesmith');
  });

  it('should update the role on save', () => {
    // given
    cy.intercept('PUT', '/api/member', { fixture: 'johndoe/p1/member-updated' }).as('updateMember');

    // when
    page.navigateTo();
    page.getEditButton('Jane Smith').click();
    page.selectMemberRole('Manager');
    page.getDialogSaveButton().click();

    // then
    cy.wait('@updateMember');
    page.getDialog().should('have.length', 0);
    page.getMemberRow('Jane Smith').should('contain.text', 'Manager');
  });

  it('should hide the dialog on cancel', () => {
    // given

    // when
    page.navigateTo();
    page.getEditButton('Jane Smith').click();
    page.getDialogCancelButton().click();

    // then
    page.getDialog().should('have.length', 0);
  });
});
