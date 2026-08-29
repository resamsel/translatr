import { KeyEditorPage } from '../../../support/project/key-editor-page.po';

describe('Project Key Editor Save', () => {
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

  it('should persist the translation on Save', () => {
    // given
    cy.intercept('PUT', '/api/message', req =>
      req.reply({ statusCode: 200, body: { ...req.body, id: 'm-new' } })
    ).as('saveMessage');

    // when
    page.navigateTo();
    page
      .getNavList()
      .find('a.locale')
      .first()
      .click();
    page
      .getEditor()
      .find('.CodeMirror textarea')
      .type('Hallo Welt', { force: true });
    cy.get('.save-button').click();

    // then
    cy.wait('@saveMessage').then(({ request }) => {
      expect(request.body.value).to.contain('Hallo Welt');
      expect(request.body.id).to.eq('6c1f75a8-903b-4251-b704-af0efd836c65');
    });
    cy.get('.save-button').should('have.text', 'Save');
    page
      .getNavList()
      .find('a.locale.active .translation')
      .should('contain.text', 'Hallo Welt');
  });

  it('should advance to the next locale on "Save and next"', () => {
    // given
    cy.intercept('/api/me?fetch=features', { fixture: 'me-save-behavior-saveandnext' });
    cy.intercept('PUT', '/api/message', req =>
      req.reply({ statusCode: 200, body: { ...req.body, id: 'm-new' } })
    ).as('saveMessage');

    // when
    page.navigateTo();
    page
      .getNavList()
      .find('a.locale')
      .first()
      .click()
      .should('have.class', 'active');
    page
      .getEditor()
      .find('.CodeMirror textarea')
      .type('Hallo Welt', { force: true });
    cy.get('.save-button').should('have.text', 'Save and next');
    cy.get('.save-button').click();

    // then
    cy.wait('@saveMessage');
    page
      .getNavList()
      .find('a.locale')
      .eq(1)
      .should('have.class', 'active');
    page
      .getNavList()
      .find('a.locale')
      .first()
      .should('not.have.class', 'active');
  });
});
