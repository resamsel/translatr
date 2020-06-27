import { Page } from '../page.po';

export class UserSettingsPage extends Page {
  constructor(private readonly username: string) {
    super();
  }

  navigateTo(): UserSettingsPage {
    cy.visit(`/${this.username}/settings`);
    return this;
  }

  getNameField(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('mat-form-field.name input');
  }

  getNameFieldError(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('mat-form-field.name mat-error');
  }

  getUsernameField(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('mat-form-field.username input');
  }

  getUsernameFieldError(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('mat-form-field.username mat-error');
  }

  getSaveButton(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('button.save');
  }
}
