import { LocaleEditorPage } from '../../../support/project/locale-editor-page.po';

describe('Project Locale Editor', () => {
  let page: LocaleEditorPage;

  beforeEach(() => {
    page = new LocaleEditorPage('johndoe', 'p1', 'default');

    cy.clearCookies();
    cy.server();

    cy.route('/api/me', 'fixture:me');
    cy.route('/api/johndoe/p1', 'fixture:johndoe/p1');
    cy.route('/api/johndoe/p1/locales/default', 'fixture:johndoe/p1/locales/default');
    cy.route('/api/project/*/locales*', 'fixture:johndoe/p1/locales');
    cy.route('/api/project/*/keys*', 'fixture:johndoe/p1/keys');
    cy.route('/api/project/*/keys?*missing=true*', 'fixture:johndoe/p1/keys-missing');
    cy.route('/api/project/*/messages*', 'fixture:johndoe/p1/messages-locale-default');
    cy.route('/api/project/*/messages?*keyName=k1', 'fixture:johndoe/p1/messages-key-k1');
    cy.route('/api/project/*/messages?*keyIds=*', 'fixture:johndoe/p1/messages-missing');
  });


  it('should have page title Language Editor', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getPageTitle()
      .should('have.text', 'Language Editor');
  });

  it('should have locale default selected in sidebar', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getSelectedLocaleField()
      .should('have.text', 'default');
  });

  it('should have two keys in sidebar', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getNavList()
      .find('.mat-list-item')
      .should('have.length', 2);
  });

  it('should have no key selected in sidebar', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getNavList()
      .find('.mat-list-item.active')
      .should('have.length', 0);
  });

  it('should have key selected when activated in sidebar', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getNavList()
      .find('.mat-list-item.key:first-of-type')
      .should('not.have.class', 'active');
    page.getNavList()
      .find('.mat-list-item.key')
      .first()
      .click()
      .should('have.class', 'active');
  });

  it('should show editor when key activated in sidebar', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getNavList()
      .find('.mat-list-item.key')
      .first()
      .click();

    page.getEditor().should('be.visible');
  });

  it('should show meta when key activated in sidebar', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getNavList()
      .find('.mat-list-item.key')
      .first()
      .click();

    page.getMeta().should('be.visible');
  });

  it('should show preview when key activated in sidebar', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getNavList()
      .find('.mat-list-item.key')
      .first()
      .click();

    page.getPreviewContents().should('have.text', 'Key One');
  });

  it('should show existing translations when translations tab selected', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getNavList()
      .find('.mat-list-item.key')
      .first()
      .click();

    page.getTranslationsTab().click();
    page.getTranslationsBody()
      .find('.mat-card')
      .should('have.length', 2);
  });

  it('should use translation when use translation is clicked', () => {
    // given

    // when
    page.navigateTo();
    page.getNavList()
      .find('.mat-list-item.key')
      .first()
      .click();

    page.getTranslationsTab().click();
    page.getTranslationsBody()
      .find('.mat-card button.use-value')
      .first()
      .click();

    // then
    page.getEditorContents().should('have.text', 'Schlüssel 1');
  });

  it('should only show keys with missing translations when filtered by those', () => {
    // given

    // when
    page.navigateTo();

    page.getFilterField().focus();
    cy.get('.autocomplete-option')
      .first()
      .trigger('click');

    // then
    cy.get('.selected-option').should('have.length', 1);
    page.getNavList()
      .find('.mat-list-item.key')
      .should('have.length', 1);
  });
});
