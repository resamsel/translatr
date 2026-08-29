import { Page } from './page.po';

export class UsersPage extends Page {
  navigateTo(): UsersPage {
    cy.visit('/users');
    return this;
  }

  getRows(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('entity-table tbody tr');
  }

  getRow(name: string): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('entity-table tbody tr').contains('tr', name);
  }

  getSearchField(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('entity-table dev-filter-field input');
  }

  getEditButton(name: string): Cypress.Chainable<JQuery<HTMLElement>> {
    return this.getRow(name).find('button').first();
  }
}
