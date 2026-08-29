import { FeatureFlagsPage } from '../support/feature-flags-page.po';

describe('Admin Feature Flags', () => {
  let page: FeatureFlagsPage;

  beforeEach(() => {
    page = new FeatureFlagsPage();

    cy.clearCookies();

    cy.intercept('/api/me*', { fixture: 'me' });
    cy.intercept('/api/featureflags*', { fixture: 'feature-flags' });
  });

  it('should render the Features page with one row per known feature', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getPageName().should('have.text', 'Features');
    page.getRows().should('have.length', 4);
    page.getToggle('language-switcher').find('mat-icon').should('have.text', 'toggle_on');
    page.getToggle('project-infographic').find('mat-icon').should('have.text', 'toggle_off');
  });

  it('should CREATE a flag (POST) when toggling a feature that has no row yet', () => {
    // given
    cy.intercept('POST', '/api/featureflag', { fixture: 'feature-flag-created' }).as('create');

    // when
    page.navigateTo();
    page.getToggle('project-infographic').click();

    // then
    cy.wait('@create')
      .its('request.body')
      .should('deep.include', { feature: 'project-infographic', enabled: true });
    page.getToggle('project-infographic').find('mat-icon').should('have.text', 'toggle_on');
    page.getToggle('project-infographic').should('have.class', 'enabled');
  });

  it('should UPDATE a flag (PUT) when toggling a feature that already has a row', () => {
    // given
    cy.intercept('PUT', '/api/featureflag', { fixture: 'feature-flag-updated' }).as('update');

    // when
    page.navigateTo();
    page.getToggle('language-switcher').click();

    // then
    cy.wait('@update')
      .its('request.body')
      .should('deep.include', { feature: 'language-switcher', enabled: false });
    page.getToggle('language-switcher').find('mat-icon').should('have.text', 'toggle_off');
    page.getToggle('language-switcher').should('have.class', 'disabled');
  });
});
