import { ProjectPage } from '../../support/project/project-page.po';

describe('Project', () => {
  let page: ProjectPage;

  beforeEach(() => {
    page = new ProjectPage('johndoe', 'p1');

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

  it('should have page name p1', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getPageName().should('have.text', 'johndoe/p1');
  });

  it('should show locale, key and member counts on the overview', () => {
    // given

    // when
    page.navigateTo();

    // then
    cy.get('app-project-info').should('exist');
    cy.get('app-project-info dev-metric.locale.count mat-card-title').should('have.text', '2');
    cy.get('app-project-info dev-metric.key.count mat-card-title').should('have.text', '2');
    cy.get('app-project-info dev-metric.member.count mat-card-title').should('have.text', '4');
  });

  it('should render the infographic when its feature flag is present', () => {
    // given
    cy.intercept('/api/me?fetch=features', { fixture: 'me-infographic' });

    // when
    page.navigateTo();

    // then
    cy.get('dev-project-infographic').should('exist');
  });

  it('should not render the infographic without its feature flag', () => {
    // given

    // when
    page.navigateTo();

    // then
    cy.get('app-project-info').should('exist');
    cy.get('dev-project-infographic').should('not.exist');
  });
});
