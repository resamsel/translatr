import { KeyEditorPage } from '../../../../support/project/key-editor-page.po';

describe('Translatr Project Key Editor', () => {
  let page: KeyEditorPage;

  beforeEach(() => {
    page = new KeyEditorPage('johndoe', 'p1', 'k1');

    cy.clearCookies();
    cy.server();

    cy.route('/api/me', 'fixture:me');
    cy.route('/api/johndoe/p1', 'fixture:project/johndoe-p1');
    cy.route('/api/johndoe/p1/keys/k1', 'fixture:project/johndoe-p1-keys-k1');
    cy.route('/api/project/*/locales*', 'fixture:project/johndoe-p1-locales');
    cy.route('/api/project/*/messages*', 'fixture:project/johndoe-p1-messages');
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
      .find('.mat-list-item.locale:first-of-type')
      .click()
      .should('have.class', 'active');
  });

  it('should show editor when locale activated in sidebar', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getNavList()
      .find('.mat-list-item.locale:first-of-type')
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
      .find('.mat-list-item.locale:first-of-type')
      .click();

    page.getMeta()
      .should('be.visible');
  });
});
