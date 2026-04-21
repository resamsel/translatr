import { ProjectSettingsPage } from '../../../support/project/project-settings-page.po';

describe('Project Settings', () => {
  let page: ProjectSettingsPage;

  beforeEach(() => {
    page = new ProjectSettingsPage('johndoe', 'p1');

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

  it('should have page name Project Settings', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getPageName().should('have.text', 'johndoe/p1');
  });

  it('should have name and description set', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getNameField().should('have.value', 'p1');
    page.getDescriptionField().should('have.value', 'p1d');
  });
});
