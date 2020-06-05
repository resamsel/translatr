import { EditorPage } from './editor-page.po';
import { ProjectKeysPage } from './project-keys-page.po';

export class KeyEditorPage extends EditorPage {
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
    return new ProjectKeysPage(this.username, this.projectName).navigateTo();
  }

  getSelectedKeyField(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('.selector input.selected-key');
  }
}
