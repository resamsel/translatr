import { DashboardPage } from './dashboard.po';
import { Page } from './page.po';

export class HomePage extends Page {
  navigateTo(): HomePage {
    cy.visit('/');
    return this;
  }

  getPageName(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('h1');
  }

  goToDashboard(): DashboardPage {
    return new DashboardPage().navigateTo();
  }

  getProjectMetricValue(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('.metric.project .mat-card-title');
  }

  getUserMetricValue(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('.metric.user .mat-card-title');
  }

  getActivityMetricValue(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('.metric.activity .mat-card-title');
  }
}
