import { Page } from '../page.po';

export class RegistrationPage extends Page {
  navigateTo(): RegistrationPage {
    cy.visit('/register');
    return this;
  }

  getNameField(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('input[formcontrolname="name"]');
  }

  getUsernameField(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('input[formcontrolname="username"]');
  }

  getUsernameError(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('dev-user-edit-form mat-error');
  }

  getSubmitButton(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('button[transloco="button.save"]');
  }

  // The Material outline label overlays the empty input, so a real click on the
  // right-hand side of the field is needed to focus it before typing.
  fillName(value: string): RegistrationPage {
    this.getNameField().click(240, 15).type(value);
    return this;
  }

  fillUsername(value: string): RegistrationPage {
    this.getUsernameField().click(240, 15).type(value).blur();
    return this;
  }
}
