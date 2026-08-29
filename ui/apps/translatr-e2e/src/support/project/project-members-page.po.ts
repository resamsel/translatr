import { Page } from '../page.po';

export class ProjectMembersPage extends Page {
  constructor(private readonly username: string, private readonly projectName: string) {
    super();
  }

  navigateTo(): ProjectMembersPage {
    cy.visit(`/${this.username}/${this.projectName}/members`);
    return this;
  }

  getMemberList(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('app-member-list app-nav-list mat-nav-list');
  }

  getMemberRows(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('app-member-list a[mat-list-item]');
  }

  getMemberRow(userName: string): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.contains('app-member-list a[mat-list-item]', userName);
  }

  getEditButton(userName: string): Cypress.Chainable<JQuery<HTMLElement>> {
    return this.getMemberRow(userName).find('button.edit');
  }

  getDeleteButton(userName: string): Cypress.Chainable<JQuery<HTMLElement>> {
    return this.getMemberRow(userName).find('confirm-button.delete');
  }

  getTransferButton(userName: string): Cypress.Chainable<JQuery<HTMLElement>> {
    return this.getMemberRow(userName).find('button.edit');
  }

  getDialogTitle(): Cypress.Chainable<JQuery<HTMLElement>> {
    return this.getDialog().find('[mat-dialog-title]');
  }

  getDialogSaveButton(): Cypress.Chainable<JQuery<HTMLElement>> {
    return this.getDialog().find('button[transloco="button.save"]');
  }

  getDialogCancelButton(): Cypress.Chainable<JQuery<HTMLElement>> {
    return this.getDialog().find('button[transloco="button.cancel"]');
  }

  fillMemberUser(search: string): ProjectMembersPage {
    this.getDialog()
      .find('input')
      .first()
      .clear()
      .type(search);
    cy.get('mat-option')
      .contains(search)
      .click();
    return this;
  }

  selectMemberRole(role: string): ProjectMembersPage {
    this.getDialog()
      .find('mat-select')
      .click();
    cy.get('.mat-mdc-select-panel mat-option')
      .contains(role)
      .click();
    return this;
  }
}
