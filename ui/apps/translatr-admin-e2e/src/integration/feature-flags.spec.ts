import { FeatureFlagsPage } from '../support/feature-flags-page.po';

describe('Admin Feature Flags', () => {
  let page: FeatureFlagsPage;

  beforeEach(() => {
    page = new FeatureFlagsPage();
    cy.clearCookies();
    cy.intercept('/api/me*', { fixture: 'me' });
    cy.intercept('/api/featureflags/resolved', { fixture: 'resolved-features' });
  });

  it('renders one row per known feature with its global-default line', () => {
    page.navigateTo();
    page.getPageName().should('have.text', 'Features');
    page.getRows().should('have.length', 4);
    page.getGlobalDefaultLine('header-graphic').should('contain.text', 'Global default: on');
    page.getGlobalDefaultLine('project-cli-card').should('contain.text', 'Global default: off');
  });

  it('CREATE: toggling a feature with no override, away from the default, POSTs enabled=true', () => {
    cy.intercept('POST', '/api/featureflag', { fixture: 'feature-flag-created' }).as('create');

    page.navigateTo();
    page.getToggle('project-infographic').click();

    cy.wait('@create')
      .its('request.body')
      .should('deep.include', { feature: 'project-infographic', enabled: true });
  });

  it('DELETE: toggling an override back to the global default removes the row', () => {
    // After the override is deleted the app re-fetches /api/featureflags/resolved;
    // that reload must reflect language-switcher back at its global default (off).
    let overrideRemoved = false;
    cy.intercept('GET', '/api/featureflags/resolved', req =>
      req.reply(
        overrideRemoved
          ? [
              { feature: 'project-cli-card', defaultEnabled: false, global: null, userOverride: null, userOverrideId: null, effective: false },
              { feature: 'project-infographic', defaultEnabled: false, global: null, userOverride: null, userOverrideId: null, effective: false },
              { feature: 'header-graphic', defaultEnabled: false, global: true, userOverride: null, userOverrideId: null, effective: true },
              { feature: 'language-switcher', defaultEnabled: false, global: null, userOverride: null, userOverrideId: null, effective: false }
            ]
          : { fixture: 'resolved-features' }
      )
    );

    cy.intercept('DELETE', '/api/featureflag/f1000000-0000-0000-0000-000000000001', req => {
      overrideRemoved = true;
      req.reply({ body: { id: 'f1000000-0000-0000-0000-000000000001' } });
    }).as('remove');

    page.navigateTo();
    // language-switcher: override ON, global default OFF → toggling off returns to default
    page.getToggle('language-switcher').click();

    cy.wait('@remove')
      .its('request.method')
      .should('equal', 'DELETE');
    page.getToggle('language-switcher').find('mat-icon').should('have.text', 'toggle_off');
  });
});
