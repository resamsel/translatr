import { Page } from '../page.po';

export class NavbarPage extends Page {
  navigateTo(): NavbarPage {
    cy.visit('/dashboard');
    return this;
  }

  getLanguageSwitcher(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('app-auth-bar-language-switcher');
  }

  getLanguageSwitcherTrigger(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('app-auth-bar-language-switcher button');
  }

  getUserMenuTrigger(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('app-auth-bar-item button.account-button');
  }

  getMenuPanel(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('.mat-mdc-menu-panel');
  }
}
