import { Page } from '../page.po';

export class ProjectActivityPage extends Page {
  constructor(private readonly username: string, private readonly projectName: string) {
    super();
  }

  navigateTo(): ProjectActivityPage {
    cy.visit(`/${this.username}/${this.projectName}/activity`);
    return this;
  }

  getActivityRows(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('app-activity-list mat-list-item');
  }

  getActivityGraph(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('dev-activity-graph');
  }
}
