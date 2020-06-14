import { Page } from '../page.po';

export class EditorPage extends Page {
  getNavList(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('.nav-list');
  }

  getNavListItems(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('.nav-list .mat-nav-list a');
  }

  getEditor(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('.editor');
  }

  getEditorContents(): Cypress.Chainable<JQuery<HTMLElement>> {
    return this.getEditor().find('.CodeMirror-code .CodeMirror-line span[role="presentation"]');
  }

  getMeta(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('.meta');
  }

  getPreviewTab(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('#mat-tab-label-0-0');
  }

  getPreviewBody(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('#mat-tab-content-0-0');
  }

  getPreviewContents(): Cypress.Chainable<JQuery<HTMLElement>> {
    return this.getPreviewBody().find('.translation');
  }

  getTranslationsTab(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('#mat-tab-label-0-1');
  }

  getTranslationsBody(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('#mat-tab-content-0-1');
  }

  getFilterField(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('dev-filter-field input');
  }
}
