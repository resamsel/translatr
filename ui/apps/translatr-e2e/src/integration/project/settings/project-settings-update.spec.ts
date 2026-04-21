import { ProjectPage } from '../../../support/project/project-page.po';
import { ProjectSettingsPage } from '../../../support/project/project-settings-page.po';

describe('Project Settings Update', () => {
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
    cy.intercept('/api/activities/aggregated*', { fixture: 'johndoe/p1/activities-aggregated' });
    cy.intercept('/api/activities*', { fixture: 'johndoe/p1/activities' });
  });

  it('should persist on save', () => {
    // given
    cy.intercept('PUT', '/api/project', { fixture: 'johndoe/p2' });
    cy.intercept('/api/johndoe/p2*', { fixture: 'johndoe/p2' });

    // when
    page.navigateTo();

    // then
    page
      .getNameField()
      .type('{selectall}p2')
      .should('have.value', 'p2');
    page
      .getDescriptionField()
      .type('{selectall}p2d')
      .should('have.value', 'p2d');
    page.getSaveButton().click();

    // cy.url().should('contain', 'johndoe/p2/settings');

    const projectPage: ProjectPage = new ProjectPage('johndoe', 'p2').navigateTo();

    projectPage.getDescription().should('have.text', 'p2d');
  });

  it('should persist with 255 chars name on save', () => {
    // given
    const name = `2${'p2'.repeat(127)}`;
    cy.intercept('PUT', '/api/project', { fixture: 'johndoe/p2-name-255' });
    cy.intercept(`/api/johndoe/${name}*`, { fixture: 'johndoe/p2-name-255' });

    // when
    page.navigateTo();

    // then
    page
      .getNameField()
      .type(`{selectall}${name}`, { delay: 0 })
      .should('have.value', name);
    page.getSaveButton().click();

    page.getPageName().should('have.text', `johndoe/${name}`);
    // cy.url().should('contain', `johndoe/${name}/settings`);
  });

  it('should not persist when name not unique', () => {
    // given
    cy.intercept({ method: 'PUT', url: '/api/project' }, { statusCode: 400, fixture: 'johndoe/p2-not-unique' });

    // when
    page.navigateTo();

    // then
    page
      .getNameField()
      .type('{selectall}p2')
      .should('have.value', 'p2');
    page.getSaveButton().click();

    page.getNameFieldError().should('be.visible');
    cy.url().should('contain', 'johndoe/p1/settings');
  });

  it('should not persist if name does not match pattern', () => {
    // given

    // when
    page.navigateTo();

    // then
    page
      .getNameField()
      .type('{selectall}p2 d2')
      .should('have.value', 'p2 d2')
      .blur();
    page.getSaveButton().should('be.disabled');

    page.getNameFieldError().should('be.visible');
  });

  it('should not persist if name is too long', () => {
    // given

    // when
    page.navigateTo();

    // then
    page
      .getNameField()
      .type('{selectall}' + 'p2'.repeat(128), { delay: 0 })
      .should('have.value', 'p2'.repeat(128));
    page.getSaveButton().should('be.disabled');
  });

  it('should not persist if description is too long', () => {
    // given
    const name = 'p2d '.repeat(501);

    // when
    page.navigateTo();

    // then
    page
      .getDescriptionField()
      .type(`{selectall}${name}`, { delay: 0.1 })
      .should('have.value', name);
    page.getSaveButton().should('be.disabled');
  });
});
