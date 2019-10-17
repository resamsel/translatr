import { DashboardPage } from './dashboard.po';
import { Page } from './page.po';

export const getGreeting = () => cy.get('h1');

export class AppPage extends Page {
  navigateTo(): AppPage {
    cy.visit('/');
    return this;
  }

  goToDashboard(): DashboardPage {
    return new DashboardPage().navigateTo();
  }
}
