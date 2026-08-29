import { Page } from '../page.po';

export class UserActivityPage extends Page {
  constructor(private readonly username: string) {
    super();
  }

  navigateTo(): UserActivityPage {
    cy.visit(`/${this.username}/activity`);
    return this;
  }

  getActivityRows(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('app-activity-list mat-list-item');
  }

  getUserLink(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('app-activity-list a.user-name');
  }

  getLoadMoreButton(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('app-activity-list a.more');
  }
}
