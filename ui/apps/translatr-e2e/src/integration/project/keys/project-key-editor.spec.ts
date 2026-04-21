import { KeyEditorPage } from '../../../support/project/key-editor-page.po';

describe('Project Key Editor', () => {
  let page: KeyEditorPage;

  beforeEach(() => {
    page = new KeyEditorPage('johndoe', 'p1', 'k1');

    cy.clearCookies();

    cy.intercept('/api/me?fetch=features', { fixture: 'me' });
    cy.intercept('/api/johndoe/p1*', { fixture: 'johndoe/p1' });
    cy.intercept('/api/johndoe/p1/keys/k1', { fixture: 'johndoe/p1/keys/k1' });
    cy.intercept('/api/project/*/locales*', { fixture: 'johndoe/p1/locales' });
    cy.intercept('/api/project/*/locales?*missing=true*', { fixture: 'johndoe/p1/locales-missing' });
    cy.intercept('/api/project/*/messages*', { fixture: 'johndoe/p1/messages' });
    cy.intercept('/api/project/*/members*', { fixture: 'johndoe/p1/members' });
    cy.intercept('/api/project/*/activities*', { fixture: 'johndoe/p1/activities' });
  });

  it('should have page title Key Editor', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getPageTitle().should('have.text', 'Key Editor');
  });

  it('should have key k1 selected in sidebar', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getSelectedKeyField().should('have.value', 'k1');
  });

  it('should have two locales in sidebar', () => {
    // given

    // when
    page.navigateTo();

    // then
    page
      .getNavList()
      .find('a.locale')
      .should('have.length', 2);
  });

  it('should have no locale selected in sidebar', () => {
    // given

    // when
    page.navigateTo();

    // then
    page
      .getNavList()
      .find('a.active')
      .should('have.length', 0);
  });

  it('should have locale selected when activated in sidebar', () => {
    // given

    // when
    page.navigateTo();

    // then
    page
      .getNavList()
      .find('a.locale:first-of-type')
      .should('not.have.class', 'active');
    page
      .getNavList()
      .find('a.locale')
      .first()
      .click()
      .should('have.class', 'active');
  });

  it('should show editor when locale activated in sidebar', () => {
    // given

    // when
    page.navigateTo();

    // then
    page
      .getNavList()
      .find('a.locale')
      .first()
      .click();

    page.getEditor().should('be.visible');
  });

  it('should show translation when locale activated in sidebar', () => {
    // given

    // when
    page.navigateTo();
    page
      .getNavList()
      .find('a.locale')
      .first()
      .click();

    // then
    page.getEditorContents().should('have.text', 'Schlüssel 1');
  });

  it('should show meta when locale activated in sidebar', () => {
    // given

    // when
    page.navigateTo();

    // then
    page
      .getNavList()
      .find('a.locale')
      .first()
      .click();

    page.getMeta().should('be.visible');
  });

  it('should show preview when locale activated in sidebar', () => {
    // given

    // when
    page.navigateTo();

    // then
    page
      .getNavList()
      .find('a.locale')
      .first()
      .click();

    page.getPreviewContents().should('have.text', 'Schlüssel 1');
  });

  it('should show existing translations when translations tab selected', () => {
    // given

    // when
    page.navigateTo();

    // then
    page
      .getNavList()
      .find('a.locale')
      .first()
      .click();

    page.getTranslationsTab().click();
    page
      .getTranslationsBody()
      .find('mat-card')
      .should('have.length', 2);
  });

  it('should use translation when use translation is clicked', () => {
    // given

    // when
    page.navigateTo();

    // then
    page
      .getNavList()
      .find('a.locale')
      .first()
      .click();

    page.getTranslationsTab().click();
    cy.get('.meta [role="tabpanel"] mat-card button.use-value')
      .last()
      .click();

    page.getEditorContents().should('have.text', 'Key One');
  });

  it('should only show locales with missing translations when filtered by those', () => {
    // given
    cy.intercept('/api/project/*/messages?*localeIds=*', { fixture: 'johndoe/p1/messages-missing' });

    // when
    page.navigateTo();

    page.getFilterField().focus();
    cy.get('.autocomplete-option')
      .first()
      .trigger('click');

    // then
    cy.get('.selected-option').should('have.length', 1);
    page
      .getNavList()
      .find('a.locale')
      .should('have.length', 1);
  });

  it('should display "Save" button when user settings say so', () => {
    // given, when
    page.navigateTo();
    page
      .getNavList()
      .find('a.locale')
      .first()
      .click();

    // then
    cy.get('.save-button').should('have.text', 'Save');
  });

  it('should display "Save and next" button when user settings say so', () => {
    // given
    cy.intercept('/api/me?fetch=features', { fixture: 'me-save-behavior-saveandnext' });

    // when
    page.navigateTo();
    page
      .getNavList()
      .find('a.locale')
      .first()
      .click();

    // then
    cy.get('.save-button').should('have.text', 'Save and next');
  });

  it('should call updateSettings on "Save and next"', () => {
    // given
    cy.intercept('PUT', '/api/message', { fixture: 'johndoe/p1/message' });
    cy.intercept('PATCH', '/api/user/*/settings', { fixture: 'me-save-behavior-saveandnext' }).as(
      'updateSettings'
    );

    // when
    page.navigateTo();
    page
      .getNavList()
      .find('a.locale')
      .first()
      .click();
    cy.get('.menu-button').click();
    cy.get('.save-behavior-saveandnext').click();

    // then
    cy.wait('@updateSettings').then(xhr => {
      expect(xhr.request.body).to.deep.equal({ 'save-behavior': 'saveandnext' });
    });
    page
      .getNavList()
      .find('a.locale')
      .eq(1)
      .should('have.class', 'active');
  });
});
