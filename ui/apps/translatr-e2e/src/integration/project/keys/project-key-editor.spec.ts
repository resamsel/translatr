import { KeyEditorPage } from '../../../support/project/key-editor-page.po';

describe('Translatr Project Key Editor', () => {
  let page: KeyEditorPage;

  beforeEach(() => {
    page = new KeyEditorPage('johndoe', 'p1', 'k1');

    cy.clearCookies();
    cy.server();

    cy.route('/api/me', 'fixture:me');
    cy.route('/api/johndoe/p1', 'fixture:johndoe/p1');
    cy.route('/api/johndoe/p1/keys/k1', 'fixture:johndoe/p1/keys/k1');
    cy.route('/api/project/*/locales*', 'fixture:johndoe/p1/locales');
    cy.route('/api/project/*/messages*', 'fixture:johndoe/p1/messages');
  });

  it('should have page title Key Editor', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getPageTitle()
      .should('have.text', 'Key Editor');
  });

  it('should have key k1 selected in sidebar', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getSelectedKeyField()
      .should('have.value', 'k1');
  });

  it('should have two locales in sidebar', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getNavList()
      .find('.mat-list-item')
      .should('have.length', 2);
  });

  it('should have no locale selected in sidebar', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getNavList()
      .find('.mat-list-item.active')
      .should('have.length', 0);
  });

  it('should have locale selected when activated in sidebar', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getNavList()
      .find('.mat-list-item.locale:first-of-type')
      .should('not.have.class', 'active');
    page.getNavList()
      .find('.mat-list-item.locale')
      .first()
      .click()
      .should('have.class', 'active');
  });

  it('should show editor when locale activated in sidebar', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getNavList()
      .find('.mat-list-item.locale')
      .first()
      .click();

    page.getEditor()
      .should('be.visible');
  });

  it('should show meta when locale activated in sidebar', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getNavList()
      .find('.mat-list-item.locale')
      .first()
      .click();

    page.getMeta()
      .should('be.visible');
  });

  it('should show preview when locale activated in sidebar', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getNavList()
      .find('.mat-list-item.locale')
      .first()
      .click();


    page.getPreviewContents()
      .should('have.text', 'Schlüssel 1');
  });

  it('should show existing translations when translations tab selected', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getNavList()
      .find('.mat-list-item.locale')
      .first()
      .click();

    page.getMeta()
      .find('#mat-tab-label-0-1')
      .click();
    page.getMeta()
      .find('#mat-tab-content-0-1 .mat-card')
      .should('have.length', 2);
  });

  it('should use translation when use translation is clicked', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getNavList()
      .find('.mat-list-item.locale')
      .first()
      .click();

    page.getMeta().within((el) => {
      el.find('#mat-tab-label-0-1')
        .click();
      el.find('#mat-tab-content-0-1 .mat-card button.use-value')
        .last()
        .click();
    });

    page.getEditorContents()
      .should('have.text', 'Key One');
  });
})
;
