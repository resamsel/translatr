import { Page } from '../page.po';

export class ProjectSettingsPage extends Page {
  constructor(
    private readonly username: string,
    private readonly projectName: string
  ) {
    super();
  }

  navigateTo(): ProjectSettingsPage {
    cy.visit(`/${this.username}/${this.projectName}/settings`);
    return this;
  }

  getNameField(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('mat-form-field.name input');
  }

  getNameFieldError(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('mat-form-field.name mat-error');
  }

  getDescriptionField(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('mat-form-field.description textarea');
  }

  getSaveButton(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('.update-project button.save');
  }

  getDeleteProjectButton(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('.delete-project button.delete');
  }

  getDeleteProjectDialog(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('app-project-delete-dialog');
  }

  getCancelProjectDeleteDialogButton(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('app-project-delete-dialog button.mat-button');
  }
}
