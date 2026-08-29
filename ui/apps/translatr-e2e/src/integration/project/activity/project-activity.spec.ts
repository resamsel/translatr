import { ProjectActivityPage } from '../../../support/project/project-activity-page.po';

describe('Project Activity', () => {
  let page: ProjectActivityPage;

  beforeEach(() => {
    page = new ProjectActivityPage('johndoe', 'p1');

    cy.clearCookies();

    cy.intercept('/api/me?fetch=features', { fixture: 'me' });
    cy.intercept('/api/johndoe/p1*', { fixture: 'johndoe/p1' });
    cy.intercept('/api/project/*/locales*', { fixture: 'johndoe/p1/locales' });
    cy.intercept('/api/project/*/keys*', { fixture: 'johndoe/p1/keys' });
    cy.intercept('/api/project/*/messages*', { fixture: 'johndoe/p1/messages' });
    cy.intercept('/api/project/*/members*', { fixture: 'johndoe/p1/members' });
    cy.intercept('/api/project/*/activities*', { fixture: 'johndoe/p1/activities' });
    cy.intercept('/api/activities*', { fixture: 'johndoe/p1/activities' });
    cy.intercept('/api/activities/aggregated*', { fixture: 'johndoe/p1/activities-aggregated' });
  });

  it('should render the project activity list and graph', () => {
    // given

    // when
    page.navigateTo();

    // then
    cy.get('app-activity-list').should('exist');
    page.getActivityGraph().should('exist');
    page.getActivityRows().should('have.length', 1);
  });
});
