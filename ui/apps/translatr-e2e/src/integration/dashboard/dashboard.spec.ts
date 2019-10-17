import { DashboardPage } from '../../support/dashboard.po';

describe('workspace-project App', () => {
  let page: DashboardPage;

  beforeEach(() => {
    page = new DashboardPage();

    cy.clearCookies();
    cy.server();
  });

  it('should have page name Dashboard', () => {
    // given
    cy.route('/api/me', 'fixture:me');
    cy.route('/api/users?limit=1&fetch=count', 'fixture:users-limit1');
    cy.route('/api/projects?owner=*', 'fixture:projects-owner-limit4');
    cy.route('/api/projects?memberId=*', 'fixture:projects-memberId-limit4');
    cy.route('/api/activities*', 'fixture:activities-userId-limit4');

    // when
    page.navigateTo();

    // then
    page.getPageName()
      .should('have.text', 'Dashboard');
    page.getProjectCardLinks()
      .should('have.length', 4);
    page.getMetric('my.project')
      .find('.mat-card-title')
      .should('have.text', '11');
    page.getMetric('my.activity')
      .find('.mat-card-title')
      .should('have.text', '1627');
    page.getMetric('all.project')
      .find('.mat-card-title')
      .should('have.text', '13');
    page.getMetric('user')
      .find('.mat-card-title')
      .should('have.text', '10352');
  });
});
