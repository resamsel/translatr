import { Page } from './page.po';

export class DashboardPage extends Page {
  navigateTo(): DashboardPage {
    cy.visit('/');
    return this;
  }

  getMetric(cls: string): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get(`dev-metric.${cls}.count`);
  }
}
