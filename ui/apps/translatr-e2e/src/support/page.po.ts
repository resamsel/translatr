export class Page {
  getPageName(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('.page');
  }

  getPageTitle(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('.title > span');
  }

  getFloatingActionButton(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('.floating-action-btn');
  }

  getDialog(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('mat-dialog-container');
  }
}
