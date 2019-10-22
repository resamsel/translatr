import { HomePage } from '../../support/app.po';

describe('Translatr Home', () => {
  let page: HomePage;

  beforeEach(() => {
    page = new HomePage();

    cy.clearCookies();
    cy.server();
  });

  it('should have page name Home', () => {
    // given
    cy.route('/api/me', 'fixture:me');

    // when
    page.navigateTo();

    // then
    page.getPageName()
      .should('have.text', 'Home');
  });
});
