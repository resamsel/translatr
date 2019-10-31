import { DashboardPage } from '../../support/dashboard.po';

describe('Dashboard Empty', () => {
  let page: DashboardPage;

  beforeEach(() => {
    page = new DashboardPage();

    cy.clearCookies();
    cy.server();

    cy.route('/api/me', 'fixture:me');
    cy.route('/api/users?limit=1&fetch=count', 'fixture:dashboard/empty/users-limit1');
    cy.route('/api/projects?owner=*', 'fixture:dashboard/empty/projects-owner-limit4');
    cy.route('/api/projects?memberId=*', 'fixture:dashboard/empty/projects-memberId-limit4');
    cy.route('/api/activities*', 'fixture:dashboard/empty/activities-userId-limit4');
  });

  it('should show teasers for empty contents', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getProjectCardLinks()
      .should('have.length', 0);
    page.getProjectEmptyView()
      .should('have.length', 1);
  });

  it('should show dialog when using teaser button', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getProjectEmptyView()
      .find('button')
      .click();
    page.getProjectCreationDialog()
      .should('be.visible');
  });

  it('should show dialog when using floating action button', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getFloatingActionButton()
      .click();
    page.getProjectCreationDialog()
      .should('be.visible');
  });
});
