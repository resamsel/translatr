import { Page } from './page.po';

export class FeatureFlagsPage extends Page {
  navigateTo(): FeatureFlagsPage {
    cy.visit('/featureflags');
    return this;
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
