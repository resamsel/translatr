import { DashboardPage } from '../support/dashboard-page.po';

describe('Admin Dashboard', () => {
  let page: DashboardPage;

  beforeEach(() => {
    page = new DashboardPage();

    cy.clearCookies();

    cy.intercept('/api/me*', { fixture: 'me' });
    cy.intercept('/api/statistics', { fixture: 'statistics' });
    cy.intercept('/api/user*', { fixture: 'users' });
    cy.intercept('/api/project*', { fixture: 'projects' });
    cy.intercept('/api/accesstokens*', { fixture: 'access-tokens' });
    cy.intercept('/api/activities*', { fixture: 'activities' });
    cy.intercept('/api/featureflags*', {
      body: { list: [], offset: 0, limit: 20, total: 0, hasPrev: false, hasNext: false }
    });
  });

  it('should render the dashboard with the page title', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getPageName().should('have.text', 'Dashboard');
  });

  it('should render the four metric cards with totals from the API', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getMetric('user').first().find('mat-card-title').should('have.text', '3');
    page.getMetric('project').first().find('mat-card-title').should('have.text', '3');
    page.getMetric('access-token').first().find('mat-card-title').should('have.text', '2');
    page.getMetric('activity').first().find('mat-card-title').should('have.text', '3');
  });

  it('should show the latest user name in the secondary metric', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getMetric('user').eq(1).find('mat-card-title').should('have.text', 'Jane Smith');
  });
});
