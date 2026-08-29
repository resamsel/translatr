import { Page } from './page.po';

export class ProjectsPage extends Page {
  navigateTo(): ProjectsPage {
    cy.visit('/projects');
    return this;
  }

  getCards(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('app-project-list a[mat-list-item]');
  }

  getSearchField(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('app-project-list dev-filter-field input');
  }

  getEmptyView(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('app-project-list dev-empty-view');
  }
}
