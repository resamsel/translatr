import { UserProjectsPage } from '../../../support/user/user-projects-page.po';

describe('User Projects More', () => {
  let page: UserProjectsPage;

  beforeEach(() => {
    page = new UserProjectsPage('janesmith');

    cy.clearCookies();

    cy.intercept('/api/me?fetch=features', { fixture: 'me' });
    cy.intercept('/api/janesmith', { fixture: 'janesmith' });
    cy.intercept('/api/projects*', { fixture: 'janesmith/projects' });
    cy.intercept('/api/activities*', { fixture: 'johndoe/activities' });
  });

  it('should show more button', () => {
    // given

    // when
    page.navigateTo();

    // then
    cy.get('a.more').should('have.length', 1);
  });
});
