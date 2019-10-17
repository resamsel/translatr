import { Page } from './page.po';

export class DashboardPage extends Page {
  navigateTo(): DashboardPage {
    cy.visit('/dashboard');
    return this;
  }

  getProjectCardLinks(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('app-project-card-link');
  }

  getMetric(kind: string): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get(`dev-metric.${kind}.count`);
  }
}

