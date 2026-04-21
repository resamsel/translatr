import { LocaleEditorPage } from '../../../support/project/locale-editor-page.po';

describe('Project Locale Editor Empty State', () => {
  let page: LocaleEditorPage;

  beforeEach(() => {
    page = new LocaleEditorPage('johndoe', 'p3', 'default');

    cy.clearCookies();

    cy.intercept('/api/me?fetch=features', { fixture: 'me' });
    cy.intercept('/api/johndoe/p3*', { fixture: 'johndoe/p3' });
    cy.intercept('/api/johndoe/p3/locales/default', { fixture: 'johndoe/p3/locales/default' });
    cy.intercept('/api/project/*/locales*', { fixture: 'johndoe/p3/locales' });
    cy.intercept('/api/project/*/keys*', { fixture: 'johndoe/p3/keys' });
    cy.intercept('/api/project/*/messages*', { fixture: 'johndoe/p3/messages' });
  });

  it('should have no keys in sidebar', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getNavListItems().should('not.exist');
  });

  it('should show key creation teaser in sidebar', () => {
    // given

    // when
    page.navigateTo();

    // then
    cy.get('dev-empty-view [data-test="key-teaser"]').should(
      'have.text',
      'It looks like there are no keys defined, yet - would you like to add one now?'
    );
    cy.get('dev-empty-view [data-test="create-key"]').should('have.text', 'Add key');
  });

  it('should show key creation dialog when clicking button', () => {
    // given

    // when
    page.navigateTo();
    cy.get('dev-empty-view [data-test="create-key"]').click();

    // then
    cy.get('mat-dialog-container').should('exist');
  });

  it('should show key creation dialog when clicking button', () => {
    // given

    // when
    page.navigateTo();
    cy.get('dev-empty-view [data-test="create-key"]').click();
    cy.get('mat-dialog-container input').type('de');
    cy.intercept('POST', '/api/key', { fixture: 'johndoe/p3/key-created' });
    cy.intercept('/api/project/*/keys*', { fixture: 'johndoe/p3/keys-added' });
    cy.get('mat-dialog-container button.save').click();

    // then
    page
      .getNavList()
      .find('a.active')
      .should('have.length', 1)
      .find('h3')
      .should('have.text', 'k1');
  });
});
