import { ProjectLocalesPage } from '../../../support/project/project-locales-page.po';

describe('Project Locales Add Locale', () => {
  let page: ProjectLocalesPage;

  beforeEach(() => {
    page = new ProjectLocalesPage('johndoe', 'p1');

    cy.clearCookies();

    cy.intercept('/api/me?fetch=features', { fixture: 'me' });
    cy.intercept('/api/johndoe/p1*', { fixture: 'johndoe/p1' });
    cy.intercept('/api/project/*/locales*', { fixture: 'johndoe/p1/locales' });
    cy.intercept('/api/project/*/keys*', { fixture: 'johndoe/p1/keys' });
    cy.intercept('/api/project/*/messages*', { fixture: 'johndoe/p1/messages' });
    cy.intercept('/api/project/*/members*', { fixture: 'johndoe/p1/members' });
    cy.intercept('/api/project/*/activities*', { fixture: 'johndoe/p1/activities' });
    cy.intercept('/api/activities/aggregated*', { fixture: 'johndoe/p1/activities-aggregated' });
  });

  it('should show locale add button', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getFloatingActionButton().should('be.visible');
  });

  it('should show locale add dialog on clicking add button', () => {
    // given

    // when
    page.navigateTo();
    page.getFloatingActionButton().click();

    // then
    page.getDialog().should('be.visible');
    page
      .getDialog()
      .find('mat-form-field.name input')
      .should('have.value', '');
    page
      .getDialog()
      .find('[mat-dialog-title]')
      .should('have.text', 'Add Language');
  });

  it('should hide locale add dialog on clicking cancel button', () => {
    // given

    // when
    page.navigateTo();
    page.getFloatingActionButton().click();
    page
      .getDialog()
      .find('button.cancel')
      .click();

    // then
    page.getDialog().should('have.length', 0);
  });

  it('should add the locale and open the new language on save', () => {
    // given
    cy.intercept('POST', '/api/locale', { fixture: 'johndoe/p1/locale-created' }).as('createLocale');
    cy.intercept('/api/project/*/locales*', { fixture: 'johndoe/p1/locales-added' });
    cy.intercept('/api/johndoe/p1/locales/fr', { fixture: 'johndoe/p1/locale-created' });

    // when
    page.navigateTo();
    page.getFloatingActionButton().click();
    page
      .getDialog()
      .find('mat-form-field.name input')
      .type('fr');
    page
      .getDialog()
      .find('button[transloco="button.save"], button.save')
      .click();

    // then
    cy.wait('@createLocale')
      .its('request.body')
      .should('deep.include', { name: 'fr' });
    page.getDialog().should('have.length', 0);
    cy.url().should('match', /\/johndoe\/p1\/locales\/fr$/);
  });
});
