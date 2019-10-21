import { ProjectSettingsPage } from '../../../support/project-settings.po';

describe('Translatr Project Settings Update', () => {
  let page: ProjectSettingsPage;

  beforeEach(() => {
    page = new ProjectSettingsPage('johndoe', 'p1');

    cy.clearCookies();
    cy.server();

    cy.route('/api/me', 'fixture:me');
    cy.route('/api/johndoe/p1', 'fixture:project/johndoe-p1');
    cy.route('/api/project/*/locales*', 'fixture:project/johndoe-p1-locales');
    cy.route('/api/project/*/keys*', 'fixture:project/johndoe-p1-keys');
    cy.route('/api/project/*/messages*', 'fixture:project/johndoe-p1-messages');
    cy.route('/api/activities/aggregated*',
      'fixture:project/johndoe-p1-activities-aggregated');
  });

  it('should persist on save', () => {
    // given
    cy.route('PUT', '/api/project', 'fixture:project/johndoe-p2');
    cy.route('/api/johndoe/p2', 'fixture:project/johndoe-p2');

    // when
    page.navigateTo();

    // then
    page.getNameField()
      .type('{selectall}p2')
      .should('have.value', 'p2');
    page.getDescriptionField()
      .type('{selectall}p2d')
      .should('have.value', 'p2d');
    page.getSaveButton().click();

    cy.url().should('contain', 'johndoe/p2/settings');
  });

  it('should persist with 255 chars name on save', () => {
    // given
    const name = `2${'p2'.repeat(127)}`;
    cy.route('PUT', '/api/project', 'fixture:project/johndoe-p2-name-255');
    cy.route(`/api/johndoe/${name}`, 'fixture:project/johndoe-p2-name-255');

    // when
    page.navigateTo();

    // then
    page.getNameField()
      .type(`{selectall}${name}`, { delay: 0 })
      .should('have.value', name);
    page.getSaveButton().click();

    cy.url().should('contain', `johndoe/${name}/settings`);
  });

  it('should not persist when name not unique', () => {
    // given
    cy.route({
      method: 'PUT',
      url: '/api/project',
      status: 400,
      response: 'fixture:project/johndoe-p2-not-unique'
    });

    // when
    page.navigateTo();

    // then
    page.getNameField()
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
    page.getNameField()
      .type('{selectall}p2 d2')
      .should('have.value', 'p2 d2')
      .blur();
    page.getSaveButton()
      .should('be.disabled');

    page.getNameFieldError().should('be.visible');
  });

  it('should not persist if name is too long', () => {
    // given
    cy.route({
      method: 'PUT',
      url: '/api/project',
      status: 400,
      response: 'fixture:project/johndoe-p2-name-too-long'
    });

    // when
    page.navigateTo();

    // then
    page.getNameField()
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
    page.getDescriptionField()
      .type(`{selectall}${name}`, { delay: 0.1 })
      .should('have.value', name);
    page.getSaveButton().should('be.disabled');
  });
});
