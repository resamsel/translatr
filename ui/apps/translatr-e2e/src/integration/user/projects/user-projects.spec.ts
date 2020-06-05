import { UserProjectsPage } from '../../../support/user/user-projects-page.po';

describe('User Projects', () => {
  let page: UserProjectsPage;

  beforeEach(() => {
    page = new UserProjectsPage('johndoe');

    cy.clearCookies();
    cy.server();

    cy.route('/api/me?fetch=features', 'fixture:me');
    cy.route('/api/johndoe', 'fixture:johndoe');
    cy.route('/api/projects*', 'fixture:johndoe/projects');
    cy.route('/api/activities*', 'fixture:johndoe/activities');
  });

  it('should show user projects', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getPageName().should('have.text', 'John Doe');
  });

  it('should not show more button', () => {
    // given

    // when
    page.navigateTo();

    // then
    cy.get('a.more').should('have.length', 0);
  });
});
