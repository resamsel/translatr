import { Page } from '../page.po';

export class LoginPage extends Page {
  navigateTo(): LoginPage {
    cy.visit('/login');
    return this;
  }

  getProviderLinks(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('.options a.client');
  }
}
