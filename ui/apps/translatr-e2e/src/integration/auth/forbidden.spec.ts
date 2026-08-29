describe('Forbidden', () => {
  beforeEach(() => {
    cy.clearCookies();

    cy.intercept('/api/me?fetch=features', { fixture: 'me' });
  });

  it('should show the forbidden page when the project API answers 403', () => {
    // given
    cy.intercept('/api/johndoe/p1*', {
      statusCode: 403,
      body: { error: { type: 'PermissionException', message: 'forbidden' } }
    });

    // when
    cy.visit('/johndoe/p1');

    // then — the AuthInterceptor routes 403 to the forbidden page (skipLocationChange)
    cy.get('.error-header').should('be.visible').and('contain.text', 'Forbidden');
  });
});
