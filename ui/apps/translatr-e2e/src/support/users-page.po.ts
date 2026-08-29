import { Page } from './page.po';

export class UsersPage extends Page {
  navigateTo(): UsersPage {
    cy.visit('/users');
    return this;
  }

  getRows(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('app-user-list a[mat-list-item]');
  }

  getSearchField(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('app-user-list dev-filter-field input');
  }

  getEmptyView(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('app-user-list dev-empty-view');
  }

  getLoadMoreButton(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('app-user-list a.more');
  }
}
