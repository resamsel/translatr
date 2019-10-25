import { ProjectSettingsPage } from '../../../support/project/project-settings-page.po';

describe('Translatr Project Settings Delete', () => {
  let page: ProjectSettingsPage;

  beforeEach(() => {
    page = new ProjectSettingsPage('johndoe', 'p1');

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

    // then
    page.getDeleteProjectButton().click();
    page.getDeleteProjectDialog().should('have.length', 1);
  });

  it('should hide the delete project dialog on cancel', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getDeleteProjectButton().click();
    page.getCancelProjectDeleteDialogButton().click();
    page.getDeleteProjectDialog().should('have.length', 0);
  });

  it('should disable the delete button when project name empty', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getDeleteProjectButton().click();
    page.getDeleteProjectDialog()
      .find('button.delete')
      .should('be.disabled');
  });

  it('should disable the delete button when project name incorrect', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getDeleteProjectButton().click();
    page.getDeleteProjectDialog()
      .find('input')
      .type('INCORRECT');
    page.getDeleteProjectDialog()
      .find('button.delete')
      .should('be.disabled');
  });

  it('should hide the delete project dialog on successful delete', () => {
    // given
    cy.route('DELETE', '/api/project/*', 'fixture:johndoe/p1');

    // dashboard
    cy.route('/api/users?limit=1&fetch=count', 'fixture:dashboard/users-limit1');
    cy.route('/api/projects?owner=*', 'fixture:dashboard/projects-owner-limit4');
    cy.route('/api/projects?memberId=*', 'fixture:dashboard/projects-memberId-limit4');
    cy.route('/api/activities*', 'fixture:dashboard/activities-userId-limit4');

    // when
    page.navigateTo();

    // then
    page.getDeleteProjectButton().click();
    page.getDeleteProjectDialog()
      .find('input')
      .type('p1');
    page.getDeleteProjectDialog()
      .find('button.delete')
      .click();
    page.getDeleteProjectDialog().should('have.length', 0);

    cy.url().should('contain', '/dashboard');
  });

  // describe('Delete Project as Manager', () => {
  //   beforeEach(() => {
  //     cy.route('/api/me', 'fixture:me');
  //   });
  //
  //   it('should have', () => {
  //     page.navigateTo();
  //   });
  // });
});
