describe('Debug project spec', () => {
  it('step 1: can visit project page', () => {
    cy.visit('/johndoe/p1');
    cy.url().should('include', 'johndoe');
  });

  it('step 2: intercept then visit', () => {
    cy.intercept('/api/johndoe/p1*', { fixture: 'johndoe/p1' });
    cy.visit('/johndoe/p1');
    cy.url().should('include', 'johndoe');
  });

  it('step 3: project/star patterns', () => {
    cy.intercept('/api/project/*/locales*', { fixture: 'johndoe/p1/locales' });
    cy.log('registered intercept successfully');
  });
});
