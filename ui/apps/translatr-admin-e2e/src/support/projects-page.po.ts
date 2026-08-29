import { Page } from './page.po';

export class ProjectsPage extends Page {
  navigateTo(): ProjectsPage {
    cy.visit('/projects');
    return this;
  }

  getRows(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('entity-table tbody tr');
  }

  getSearchField(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('entity-table dev-filter-field input');
  }
}
