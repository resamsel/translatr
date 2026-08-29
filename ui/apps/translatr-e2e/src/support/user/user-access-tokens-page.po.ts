import { Page } from '../page.po';

export class UserAccessTokensPage extends Page {
  constructor(private readonly username: string) {
    super();
  }

  navigateTo(): UserAccessTokensPage {
    cy.visit(`/${this.username}/access-tokens`);
    return this;
  }

  getRows(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('app-user-access-tokens a[mat-list-item]');
  }

  getEmptyView(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('app-user-access-tokens dev-empty-view');
  }
}
