import { ProjectSettingsPage } from '../../../support/project/project-settings-page.po';

describe('Project Settings Delete', () => {
  let page: ProjectSettingsPage;

  beforeEach(() => {
    page = new ProjectSettingsPage('johndoe', 'p1');

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

  it('should show the delete project button', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getDeleteProjectButton().should('be.visible');
  });

  it('should show the delete project dialog when on delete project', () => {
    // given

    // when
    page.navigateTo();
    page.getDeleteProjectButton().click();

    // then
    page.getDeleteProjectDialog().should('have.length', 1);
  });

  it('should hide the delete project dialog on cancel', () => {
    // given

    // when
    page.navigateTo();
    page.getDeleteProjectButton().click();
    page.getCancelProjectDeleteDialogButton().click();

    // then
    page.getDeleteProjectDialog().should('have.length', 0);
  });

  it('should disable the delete button when project name empty', () => {
    // given

    // when
    page.navigateTo();
    page.getDeleteProjectButton().click();

    // then
    page
      .getDeleteProjectDialog()
      .find('button.delete')
      .should('be.disabled');
  });

  it('should disable the delete button when project name incorrect', () => {
    // given

    // when
    page.navigateTo();
    page.getDeleteProjectButton().click();
    page
      .getDeleteProjectDialog()
      .find('input')
      .type('INCORRECT');

    // then
    page
      .getDeleteProjectDialog()
      .find('button.delete')
      .should('be.disabled');
  });

  it('should hide the delete project dialog on successful delete', () => {
    // given
    cy.intercept('DELETE', '/api/project/*', { fixture: 'johndoe/p1' });

    // dashboard
    cy.intercept('/api/users?limit=1&fetch=count', { fixture: 'dashboard/users-limit1' });
    cy.intercept('/api/projects?owner=*', { fixture: 'dashboard/projects-owner-limit4' });
    cy.intercept('/api/projects?memberId=*', { fixture: 'dashboard/projects-memberId-limit4' });
    cy.intercept('/api/activities*', { fixture: 'dashboard/activities-userId-limit4' });

    // when
    page.navigateTo();
    page.getDeleteProjectButton().click();
    page
      .getDeleteProjectDialog()
      .find('input')
      .type('p1');
    page
      .getDeleteProjectDialog()
      .find('button.delete')
      .click();

    // then
    page.getDeleteProjectDialog().should('have.length', 0);

    cy.url().should('contain', '/dashboard');
  });

  // describe('Delete Project as Manager', () => {
  //   beforeEach(() => {
  //     cy.intercept('/api/me?fetch=features', { fixture: 'me' });
  //   });
  //
  //   it('should have', () => {
  //     page.navigateTo();
  //   });
  // });
});
