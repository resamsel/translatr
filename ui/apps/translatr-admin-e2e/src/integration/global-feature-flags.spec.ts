import { FeatureFlagsPage } from '../support/feature-flags-page.po';

describe('Admin Global Feature Flags', () => {
  let page: FeatureFlagsPage;

  beforeEach(() => {
    page = new FeatureFlagsPage();
    cy.clearCookies();
    cy.intercept('/api/me*', { fixture: 'me' });
    cy.intercept('/api/featureflags/resolved', { fixture: 'resolved-features' });
    cy.intercept('/api/featureflags/global', { fixture: 'global-feature-flags' });
  });

  it('renders one row per known feature with its default', () => {
    page.navigateToGlobal();
    page.getPageName().should('have.text', 'Feature Flags');
    page.getActiveTab().should('contain.text', 'Global');
    page.getRows().should('have.length', 4);
    page.getToggle('header-graphic').find('mat-icon').should('have.text', 'toggle_on');
    page.getToggle('language-switcher').find('mat-icon').should('have.text', 'toggle_off');
  });

  it('POSTs feature + enabled when toggling a feature on globally', () => {
    cy.intercept('POST', '/api/featureflag/global', { fixture: 'global-feature-flag-set' }).as('set');

    page.navigateToGlobal();
    page.getToggle('language-switcher').click();

    cy.wait('@set').its('request.body').should('deep.equal', {
      feature: 'language-switcher',
      enabled: true
    });
  });

  it('POSTs enabled=false when disabling a globally-enabled feature', () => {
    cy.intercept('POST', '/api/featureflag/global', {
      body: { id: 'a0000000-0000-0000-0000-000000000001', feature: 'header-graphic', enabled: false }
    }).as('set');

    page.navigateToGlobal();
    page.getToggle('header-graphic').click();

    cy.wait('@set').its('request.body').should('deep.equal', {
      feature: 'header-graphic',
      enabled: false
    });
  });
});
