import { NavbarPage } from '../../support/nav/navbar.po';

describe('Navbar', () => {
  let page: NavbarPage;

  beforeEach(() => {
    page = new NavbarPage();

    cy.clearCookies();

    cy.intercept('/api/me?fetch=features', { fixture: 'me' });
    cy.intercept('/api/users?limit=1&fetch=count', { fixture: 'dashboard/users-limit1' });
    cy.intercept('/api/projects?owner=*', { fixture: 'dashboard/projects-owner-limit4' });
    cy.intercept('/api/projects?memberId=*', { fixture: 'dashboard/projects-memberId-limit4' });
    cy.intercept('/api/activities*', { fixture: 'dashboard/activities-userId-limit4' });
  });

  it('should switch the active language via the language switcher', () => {
    // given
    cy.intercept('/api/me?fetch=features', { fixture: 'me-language-switcher' });

    // when
    page.navigateTo();
    page.getLanguageSwitcher().should('exist');
    page.getLanguageSwitcherTrigger().click();

    // then
    page
      .getMenuPanel()
      .find('button[mat-menu-item]')
      .should('have.length', 2);
    page
      .getMenuPanel()
      .find('button[mat-menu-item]')
      .eq(0)
      .should('have.class', 'active');
    page
      .getMenuPanel()
      .find('button[mat-menu-item]')
      .eq(1)
      .should('not.have.class', 'active');

    // when — switch to the second language
    page
      .getMenuPanel()
      .find('button[mat-menu-item]')
      .eq(1)
      .click();
    page.getLanguageSwitcherTrigger().click();

    // then — the second language is now active
    page
      .getMenuPanel()
      .find('button[mat-menu-item]')
      .eq(1)
      .should('have.class', 'active');
    page
      .getMenuPanel()
      .find('button[mat-menu-item]')
      .eq(0)
      .should('not.have.class', 'active');
  });

  it('should expose profile and logout in the user menu', () => {
    // given

    // when
    page.navigateTo();
    page.getUserMenuTrigger().click();

    // then
    page.getMenuPanel().should('be.visible');
    page
      .getMenuPanel()
      .find('[transloco="user.profile"]')
      .should('have.attr', 'href')
      .and('match', /\/johndoe$/);
    page
      .getMenuPanel()
      .find('[transloco="auth.logout"]')
      .should('have.attr', 'href')
      .and('include', '/logout');
  });
});
