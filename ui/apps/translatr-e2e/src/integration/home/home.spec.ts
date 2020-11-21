import { HomePage } from '../../support/app.po';

describe('Home', () => {
  let page: HomePage;

  beforeEach(() => {
    page = new HomePage();

    cy.clearCookies();
    cy.server();
  });

  it('should have page name Home', () => {
    // given
    cy.route('/api/me?fetch=features', 'fixture:me');
    cy.route('/api/statistics', 'fixture:statistics');
    cy.route('/api/activities/aggregated', 'fixture:activities-aggregated');

    // when
    page.navigateTo();

    // then
    page.getPageName().should('have.text', 'Translatr');
  });

  it('should have project metric value', () => {
    // given
    cy.route('/api/me?fetch=features', 'fixture:me');
    cy.route('/api/statistics', 'fixture:statistics');
    cy.route('/api/activities/aggregated', 'fixture:activities-aggregated');

    // when
    page.navigateTo();

    // then
    page.getProjectMetricValue().should('have.text', '15');
  });

  it('should have user metric value', () => {
    // given
    cy.route('/api/me?fetch=features', 'fixture:me');
    cy.route('/api/statistics', 'fixture:statistics');
    cy.route('/api/activities/aggregated', 'fixture:activities-aggregated');

    // when
    page.navigateTo();

    // then
    page.getUserMetricValue().should('have.text', '25');
  });

  it('should have activity metric value', () => {
    // given
    cy.route('/api/me?fetch=features', 'fixture:me');
    cy.route('/api/statistics', 'fixture:statistics');
    cy.route('/api/activities/aggregated', 'fixture:activities-aggregated');

    // when
    page.navigateTo();

    // then
    page.getActivityMetricValue().should('have.text', '100');
  });
});
