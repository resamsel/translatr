import { KeyEditorPage } from '../../../support/project/key-editor-page.po';

describe('Project Key Editor Empty State', () => {
  let page: KeyEditorPage;

  beforeEach(() => {
    page = new KeyEditorPage('johndoe', 'p0', 'k1');

    cy.clearCookies();
    cy.server();

    cy.route('/api/me?fetch=features', 'fixture:me');
    cy.route('/api/johndoe/p0*', 'fixture:johndoe/p0');
    cy.route('/api/johndoe/p0/keys/k1', 'fixture:johndoe/p0/keys/k1');
    cy.route('/api/project/*/locales*', 'fixture:johndoe/p0/locales');
    cy.route('/api/project/*/locales?*missing=true*', 'fixture:johndoe/p0/locales-missing');
    cy.route('/api/project/*/messages*', 'fixture:johndoe/p0/messages');
    cy.route('/api/project/*/members*', 'fixture:johndoe/p0/members');
    cy.route('/api/project/*/activities*', 'fixture:johndoe/p0/activities');
  });

  it('should have no languages in sidebar', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getNavListItems().should('not.exist');
  });

  it('should show language creation teaser in sidebar', () => {
    // given

    // when
    page.navigateTo();

    // then
    cy.get('dev-empty-view [data-test="locale-teaser"]').should(
      'have.text',
      'It looks like there are no languages defined, yet - would you like to add one now?'
    );
    cy.get('dev-empty-view [data-test="create-locale"]').should('have.text', 'Add language');
  });

  it('should show language creation dialog when clicking button', () => {
    // given

    // when
    page.navigateTo();
    cy.get('dev-empty-view [data-test="create-locale"]').click();

    // then
    cy.get('mat-dialog-container').should('exist');
  });
});
