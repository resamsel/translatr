import { UserProjectsPage } from '../../../support/user/user-projects-page.po';

describe('User Projects More', () => {
  let page: UserProjectsPage;

  beforeEach(() => {
    page = new UserProjectsPage('janesmith');

    cy.clearCookies();
    cy.server();

    cy.route('/api/me', 'fixture:me');
    cy.route('/api/janesmith', 'fixture:janesmith');
    cy.route('/api/projects*', 'fixture:janesmith/projects');
    cy.route('/api/activities*', 'fixture:johndoe/activities');
  });

  it('should show more button', () => {
    // given

    // when
    page.navigateTo();

    // then
    cy.get('a.more').should('have.length', 1);
  });
});
