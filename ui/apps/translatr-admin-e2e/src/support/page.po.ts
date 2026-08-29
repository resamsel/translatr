export class Page {
  getPageName(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('h1.page');
  }

  getFloatingActionButton(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('.floating-action-btn');
  }

  getDialog(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('mat-dialog-container');
  }
}
