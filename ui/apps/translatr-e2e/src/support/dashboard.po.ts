import { Page } from './page.po';

export class DashboardPage extends Page {
  navigateTo(): DashboardPage {
    cy.visit('/dashboard');
    return this;
  }

  getProjectCardLinks(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('app-project-card-link');
  }

  getProjectEmptyView(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('app-project-empty-view');
  }

  getMetric(kind: string): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get(`dev-metric.${kind}.count`);
  }

  getProjectCreationDialog(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('app-protect-creation-dialog');
  }
}
