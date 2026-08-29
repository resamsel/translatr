import { UserActivityPage } from '../../../support/user/user-activity-page.po';

describe('User Activity', () => {
  let page: UserActivityPage;

  beforeEach(() => {
    page = new UserActivityPage('johndoe');

    cy.clearCookies();

    cy.intercept('/api/me?fetch=features', { fixture: 'me' });
    cy.intercept('/api/johndoe', { fixture: 'johndoe' });
    cy.intercept('/api/user/*/activity*', { fixture: 'johndoe/activities' });
    cy.intercept('/api/activities*', { fixture: 'johndoe/activities' });
    cy.intercept('/api/activities/aggregated*', { fixture: 'activities-aggregated' });
  });

  it('should render the activity list', () => {
    // given

    // when
    page.navigateTo();

    // then
    cy.get('app-activity-list').should('exist');
    page.getActivityRows().should('have.length', 3);
  });

  it('should navigate to the user on an activity link click', () => {
    // given
    cy.intercept('/api/projects*', { fixture: 'johndoe/projects' });

    // when
    page.navigateTo();
    page.getUserLink().first().click();

    // then
    cy.url().should('match', /\/johndoe$/);
  });

  it('should load more activities', () => {
    // given
    cy.intercept('/api/activities*limit=8*', { fixture: 'johndoe/activities-page2' }).as('more');

    // when
    page.navigateTo();
    page.getActivityRows().should('have.length', 3);
    page.getLoadMoreButton().click();

    // then
    cy.wait('@more').its('request.url').should('contain', 'limit=8');
    page.getActivityRows().should('have.length', 2);
  });
});
