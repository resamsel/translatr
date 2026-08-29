import { Page } from '../page.po';

export class UserAccessTokenPage extends Page {
  constructor(private readonly username: string, private readonly id: string = 'create') {
    super();
  }

  navigateTo(): UserAccessTokenPage {
    cy.visit(`/${this.username}/access-tokens/${this.id}`);
    return this;
  }

  getNameField(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('input[formcontrolname="name"]');
  }

  getSaveButton(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('button[transloco="button.save"]');
  }

  getCancelLink(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('a[transloco="button.cancel"]');
  }

  getSecret(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('.key input[formcontrolname="key"]');
  }
}
