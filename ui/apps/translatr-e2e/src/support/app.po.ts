import { DashboardPage } from './dashboard.po';
import { Page } from './page.po';

export class HomePage extends Page {
  navigateTo(): HomePage {
    cy.visit('/');
    return this;
  }

  goToDashboard(): DashboardPage {
    return new DashboardPage().navigateTo();
  }
}
