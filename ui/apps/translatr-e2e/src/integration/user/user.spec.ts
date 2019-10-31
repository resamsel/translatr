import { UserPage } from '../../support/user/user-page.po';

describe('User Page', () => {
  let page: UserPage;

  beforeEach(() => {
    page = new UserPage('johndoe');

    cy.clearCookies();
    cy.server();

    cy.route('/api/me', 'fixture:me');
    cy.route('/api/johndoe', 'fixture:johndoe');
    cy.route('/api/projects*', 'fixture:johndoe/projects');
    cy.route('/api/activities*', 'fixture:johndoe/activities');
  });

  it('should show user page', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getPageName()
      .should('have.text', 'John Doe');
  });
});
