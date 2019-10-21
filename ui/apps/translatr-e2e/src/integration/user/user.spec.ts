import { UserPage } from '../../support/user-page.po';

describe('Translatr User Page', () => {
  let page: UserPage;

  beforeEach(() => {
    page = new UserPage('johndoe');

    cy.clearCookies();
    cy.server();

    cy.route('/api/me', 'fixture:me');
    cy.route('/api/johndoe', 'fixture:user/johndoe');
    cy.route('/api/projects*', 'fixture:user/johndoe-projects');
    cy.route('/api/activities*', 'fixture:user/johndoe-activities');
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
