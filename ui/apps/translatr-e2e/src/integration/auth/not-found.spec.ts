describe('Not Found', () => {
  beforeEach(() => {
    cy.clearCookies();

    cy.intercept('/api/me?fetch=features', { fixture: 'me' });
    cy.intercept('/api/me*', { fixture: 'me' });
  });

  it('should show the not-found page when the project API answers 404', () => {
    // given
    cy.intercept('/api/johndoe/nope*', {
      statusCode: 404,
      body: { error: { type: 'NotFoundException', entity: 'Project' } }
    });

    // when
    cy.visit('/johndoe/nope');

    // then — the AuthInterceptor routes 404 to the not-found page (skipLocationChange)
    cy.get('.error-header').should('be.visible').and('contain.text', 'Page not found');
  });

  it('should land on /not-found for an unknown top-level route', () => {
    // given
    cy.intercept('/api/this-route-does-not-exist*', { statusCode: 200, body: null });

    // when
    cy.visit('/this-route-does-not-exist', { failOnStatusCode: false });

    // then — UserGuard cannot resolve the username and redirects to /not-found
    cy.url().should('contain', '/not-found');
    cy.get('.error-header').should('be.visible').and('contain.text', 'Page not found');
  });
});
