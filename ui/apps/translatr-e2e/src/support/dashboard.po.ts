import { Page } from './page.po';

export class DashboardPage extends Page {
  navigateTo(): DashboardPage {
    cy.visit('/dashboard');
    return this;
  }
}

