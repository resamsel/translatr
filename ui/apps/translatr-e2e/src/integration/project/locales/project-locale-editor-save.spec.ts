import { LocaleEditorPage } from '../../../support/project/locale-editor-page.po';

describe('Project Locale Editor Save', () => {
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
    cy.intercept('/api/project/*/messages?*keyIds=*', {
      fixture: 'johndoe/p1/messages-locale-default'
    });
    cy.intercept('/api/project/*/members*', { fixture: 'johndoe/p1/members' });
    cy.intercept('/api/project/*/activities*', { fixture: 'johndoe/p1/activities' });
  });

  it('should persist the translation on Save', () => {
    // given
    cy.intercept('PUT', '/api/message', req =>
      req.reply({ statusCode: 200, body: { ...req.body, id: 'm-new' } })
    ).as('saveMessage');

    // when
    page.navigateTo();
    page
      .getNavList()
      .find('a.key')
      .first()
      .click();
    page
      .getEditor()
      .find('.CodeMirror textarea')
      .type('Bonjour', { force: true });
    cy.get('.save-button').click();

    // then
    cy.wait('@saveMessage').then(({ request }) => {
      expect(request.body.value).to.contain('Bonjour');
      expect(request.body.id).to.eq('d8a87a83-ad7f-42b5-ade4-94a0e5cf54b3');
    });
    cy.get('.save-button').should('have.text', 'Save');
    page
      .getNavList()
      .find('a.key.active .translation')
      .should('contain.text', 'Bonjour');
  });

  it('should advance to the next key on "Save and next"', () => {
    // given
    cy.intercept('/api/me?fetch=features', { fixture: 'me-save-behavior-saveandnext' });
    cy.intercept('PUT', '/api/message', req =>
      req.reply({ statusCode: 200, body: { ...req.body, id: 'm-new' } })
    ).as('saveMessage');

    // when
    page.navigateTo();
    page
      .getNavList()
      .find('a.key')
      .first()
      .click()
      .should('have.class', 'active');
    page
      .getEditor()
      .find('.CodeMirror textarea')
      .type('Bonjour', { force: true });
    cy.get('.save-button').should('have.text', 'Save and next');
    cy.get('.save-button').click();

    // then
    cy.wait('@saveMessage');
    page
      .getNavList()
      .find('a.key')
      .eq(1)
      .should('have.class', 'active');
    page
      .getNavList()
      .find('a.key')
      .first()
      .should('not.have.class', 'active');
  });
});
