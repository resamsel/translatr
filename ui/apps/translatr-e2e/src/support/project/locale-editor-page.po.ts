import { ProjectLocalesPage } from './project-locales-page.po';
import { EditorPage } from './editor-page.po';

export class LocaleEditorPage extends EditorPage {
  constructor(
    public readonly username: string,
    public readonly projectName: string,
    public readonly localeName: string
  ) {
    super();
  }

  navigateTo(): LocaleEditorPage {
    cy.visit(`/${this.username}/${this.projectName}/locales/${this.localeName}`);
    return this;
  }

  navigateToLocales(): ProjectLocalesPage {
    return new ProjectLocalesPage(this.username, this.projectName)
      .navigateTo();
  }

  getSelectedLocaleField(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('.selector .selected-locale');
  }
}
