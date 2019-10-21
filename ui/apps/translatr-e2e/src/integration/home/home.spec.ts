import { AppPage } from '../../support/app.po';

describe('Translatr Home', () => {
  let page: AppPage;

  beforeEach(() => {
    page = new AppPage();

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
