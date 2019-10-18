import { DashboardPage } from '../../support/dashboard.po';

describe('Translatr Dashboard', () => {
  let page: DashboardPage;

  beforeEach(() => {
    page = new DashboardPage();

    cy.clearCookies();
    cy.server();

    cy.route('/api/me', 'fixture:me');
    cy.route('/api/users?limit=1&fetch=count', 'fixture:dashboard/users-limit1');
    cy.route('/api/projects?owner=*', 'fixture:dashboard/projects-owner-limit4');
    cy.route('/api/projects?memberId=*', 'fixture:dashboard/projects-memberId-limit4');
    cy.route('/api/activities*', 'fixture:dashboard/activities-userId-limit4');
  });

  it('should have page name Dashboard', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getPageName()
      .should('have.text', 'Dashboard');
  });

  it('should have metrics values', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getProjectCardLinks()
      .should('have.length', 4);
    page.getProjectEmptyView()
      .should('have.length', 0);
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
