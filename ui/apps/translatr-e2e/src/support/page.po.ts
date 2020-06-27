export class Page {
  getPageName(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('.page');
  }

  getTitle(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('title');
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
