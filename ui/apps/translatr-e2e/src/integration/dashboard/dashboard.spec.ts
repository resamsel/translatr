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

    // when
    page.navigateTo();

    // then
    page.getPageName()
      .should('have.text', 'Dashboard');
  });
});
