import { Page } from './page.po';

export class FeatureFlagsPage extends Page {
  /** The tabbed Feature Flags page opens on the "User" tab. */
  navigateTo(): FeatureFlagsPage {
    cy.visit('/featureflags');
    return this;
  }

  /** Open the page and switch to the "Global" tab. */
  navigateToGlobal(): FeatureFlagsPage {
    this.navigateTo();
    this.selectTab('Global');
    return this;
  }

  selectTab(label: string): FeatureFlagsPage {
    cy.get('[role="tab"]').contains(label).click();
    return this;
  }

  getActiveTab(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('[role="tab"][aria-selected="true"]');
  }

  getGlobalDefaultLine(featureKey: string): Cypress.Chainable<JQuery<HTMLElement>> {
    return this.getRow(featureKey).find('.feature-name .sub-title').last();
  }

  getRows(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('.feature-row');
  }

  /**
   * Matches a feature row on the raw {@link Feature} enum value rendered in
   * `.feature-name .sub-title` (e.g. `language-switcher`).
   */
  getRow(featureKey: string): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('.feature-row').contains('.feature-row', featureKey);
  }

  getToggle(featureKey: string): Cypress.Chainable<JQuery<HTMLElement>> {
    return this.getRow(featureKey).find('.feature-actions button');
  }
}
