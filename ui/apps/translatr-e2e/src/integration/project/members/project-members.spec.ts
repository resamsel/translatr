import { ProjectMembersPage } from '../../../support/project/project-members-page.po';

describe('Project Members', () => {
  let page: ProjectMembersPage;

  beforeEach(() => {
    page = new ProjectMembersPage('johndoe', 'p1');

    cy.clearCookies();

    cy.intercept('/api/me?fetch=features', { fixture: 'me' });
    cy.intercept('/api/johndoe/p1*', { fixture: 'johndoe/p1' });
    cy.intercept('/api/project/*/locales*', { fixture: 'johndoe/p1/locales' });
    cy.intercept('/api/project/*/keys*', { fixture: 'johndoe/p1/keys' });
    cy.intercept('/api/project/*/messages*', { fixture: 'johndoe/p1/messages' });
    cy.intercept('/api/project/*/members*', { fixture: 'johndoe/p1/members' });
    cy.intercept('/api/project/*/activities*', { fixture: 'johndoe/p1/activities' });
    cy.intercept('/api/activities/aggregated*', { fixture: 'johndoe/p1/activities-aggregated' });
  });

  it('should have page name johndoe/p1', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getPageName().should('have.text', 'johndoe/p1');
  });

  it('should render all four members with name and role', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getMemberRows().should('have.length', 4);
    page.getMemberRow('John Doe').should('contain.text', 'Owner');
    page.getMemberRow('Jane Smith').should('contain.text', 'Developer');
    page.getMemberRow("Sophia O'Reilly").should('contain.text', 'Translator');
    page.getMemberRow('Ronny Lee').should('contain.text', 'Manager');
  });
});
