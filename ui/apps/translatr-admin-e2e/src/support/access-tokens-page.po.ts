import { Page } from './page.po';

export class AccessTokensPage extends Page {
  navigateTo(): AccessTokensPage {
    cy.visit('/accesstokens');
    return this;
  }

  getRows(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('entity-table tbody tr');
  }

  getEditButton(name: string): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('entity-table tbody tr').contains('tr', name).find('button').first();
  }
}
