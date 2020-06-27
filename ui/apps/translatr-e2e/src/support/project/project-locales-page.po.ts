import { Page } from '../page.po';

export class ProjectLocalesPage extends Page {
  constructor(private readonly username: string, private readonly projectName: string) {
    super();
  }

  navigateTo(): ProjectLocalesPage {
    cy.visit(`/${this.username}/${this.projectName}/locales`);
    return this;
  }

  getLocaleList(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('app-locale-list .mat-nav-list');
  }
}
