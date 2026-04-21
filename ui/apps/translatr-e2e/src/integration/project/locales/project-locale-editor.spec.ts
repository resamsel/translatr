import { LocaleEditorPage } from '../../../support/project/locale-editor-page.po';

describe('Project Locale Editor', () => {
  let page: LocaleEditorPage;

  beforeEach(() => {
    page = new LocaleEditorPage('johndoe', 'p1', 'default');

    cy.clearCookies();

    cy.intercept('/api/me?fetch=features', { fixture: 'me' });
    cy.intercept('/api/johndoe/p1*', { fixture: 'johndoe/p1' });
    cy.intercept('/api/johndoe/p1/locales/default', { fixture: 'johndoe/p1/locales/default' });
    cy.intercept('/api/project/*/locales*', { fixture: 'johndoe/p1/locales' });
    cy.intercept('/api/project/*/keys*', { fixture: 'johndoe/p1/keys' });
    cy.intercept('/api/project/*/keys?*missing=true*', { fixture: 'johndoe/p1/keys-missing' });
    cy.intercept('/api/project/*/messages*', { fixture: 'johndoe/p1/messages-locale-default' });
    cy.intercept('/api/project/*/messages?*keyName=k1', { fixture: 'johndoe/p1/messages-key-k1' });
    cy.intercept('/api/project/*/messages?*keyIds=*', { fixture: 'johndoe/p1/messages-locale-default' });
    cy.intercept('/api/project/*/members*', { fixture: 'johndoe/p1/members' });
    cy.intercept('/api/project/*/activities*', { fixture: 'johndoe/p1/activities' });
  });

  it('should have page title Language Editor', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getPageTitle().should('have.text', 'Language Editor');
  });

  it('should have locale default selected in sidebar', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getSelectedLocaleField().should('have.text', 'default');
  });

  it('should have two keys in sidebar', () => {
    // given

    // when
    page.navigateTo();

    // then
    page
      .getNavList()
      .find('a.key')
      .should('have.length', 2);
  });

  it('should have no key selected in sidebar', () => {
    // given

    // when
    page.navigateTo();

    // then
    page
      .getNavList()
      .find('a.active')
      .should('have.length', 0);
  });

  it('should have key selected when activated in sidebar', () => {
    // given

    // when
    page.navigateTo();

    // then
    page
      .getNavList()
      .find('a.key:first-of-type')
      .should('not.have.class', 'active');
    page
      .getNavList()
      .find('a.key')
      .first()
      .click()
      .should('have.class', 'active');
  });

  it('should show editor when key activated in sidebar', () => {
    // given

    // when
    page.navigateTo();

    // then
    page
      .getNavList()
      .find('a.key')
      .first()
      .click();

    page.getEditor().should('be.visible');
  });

  it('should show translation when key activated in sidebar', () => {
    // given

    // when
    page.navigateTo();
    page
      .getNavList()
      .find('a.key')
      .first()
      .click();

    // then
    page.getEditorContents().should('have.text', 'Key One');
  });

  it('should show meta when key activated in sidebar', () => {
    // given

    // when
    page.navigateTo();

    // then
    page
      .getNavList()
      .find('a.key')
      .first()
      .click();

    page.getMeta().should('be.visible');
  });

  it('should show preview when key activated in sidebar', () => {
    // given
    cy.intercept('/api/project/*/messages?*keyIds=*', { fixture: 'johndoe/p1/messages-locale-default' });

    // when
    page.navigateTo();
    page
      .getNavList()
      .find('a.key')
      .first()
      .click();

    // then
    page.getPreviewContents().should('have.text', 'Key One');
  });

  it('should show existing translations when translations tab selected', () => {
    // given

    // when
    page.navigateTo();

    // then
    page
      .getNavList()
      .find('a.key')
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
    page
      .getNavList()
      .find('a.key')
      .first()
      .click();

    page.getTranslationsTab().click();
    cy.get('.meta [role="tabpanel"] mat-card button.use-value')
      .first()
      .click();

    // then
    page.getEditorContents().should('have.text', 'Schlüssel 1');
  });

  it('should only show keys with missing translations when filtered by those', () => {
    // given
    cy.intercept('/api/project/*/messages?*keyIds=*', { fixture: 'johndoe/p1/messages-missing' });

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
      .find('a.key')
      .should('have.length', 1);
  });

  it('should display "Save" button when user settings say so', () => {
    // given, when
    page.navigateTo();
    page
      .getNavList()
      .find('a.key')
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
      .find('a.key')
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
      .find('a.key')
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
      .find('a.key')
      .eq(1)
      .should('have.class', 'active');
  });
});
