import { DashboardPage } from '../../support/dashboard.po';

describe('Translatr Dashboard Forbidden', () => {
  let page: DashboardPage;

  beforeEach(() => {
    page = new DashboardPage();

    cy.clearCookies();
    cy.server();

    cy.route('/api/me', 'fixture:me-null');
  });

  it('should redirect to forbidden page', () => {
    // given

    // when
    page.navigateTo();

    // then
  });
});
