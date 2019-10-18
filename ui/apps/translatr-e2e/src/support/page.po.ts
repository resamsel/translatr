export class Page {
  getPageName() {
    return cy.get('.page');
  }

  getFloatingActionButton(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('.floating-action-button');
  }
}
