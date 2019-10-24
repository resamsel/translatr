import { Page } from '../page.po';
import { ProjectKeysPage } from './project-keys-page.po';

export class KeyEditorPage extends Page {
  constructor(
    public readonly username: string,
    public readonly projectName: string,
    public readonly keyName: string
  ) {
    super();
  }

  navigateTo(): KeyEditorPage {
    cy.visit(`/${this.username}/${this.projectName}/keys/${this.keyName}`);
    return this;
  }

  navigateToKeys(): ProjectKeysPage {
    return new ProjectKeysPage(this.username, this.projectName)
      .navigateTo();
  }

  getNavList(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('.nav-list');
  }

  getSelectedKeyField(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('.selector input.selected-key');
  }

  getEditor(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('.editor');
  }

  getMeta(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('.meta');
  }
}
