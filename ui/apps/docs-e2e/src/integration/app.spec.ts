import { getGreeting } from '../support/app.po';

describe('docs', () => {
  beforeEach(() => cy.visit('/'));

  it('should display welcome message', () => {
    getGreeting().contains('Welcome to docs!');
  });
});
