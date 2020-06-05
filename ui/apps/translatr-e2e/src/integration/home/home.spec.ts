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

    // when
    page.navigateTo();

    // then
    page.getPageName().should('have.text', 'Translatr');
  });
});
