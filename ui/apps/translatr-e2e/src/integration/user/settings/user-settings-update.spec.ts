import { UserSettingsPage } from '../../../support/user/user-settings-page.po';

describe('User Settings Update', () => {
  let page: UserSettingsPage;

  beforeEach(() => {
    page = new UserSettingsPage('johndoe');

    cy.clearCookies();
    cy.server();

    cy.route('/api/me', 'fixture:johndoe');
    cy.route('/api/johndoe', 'fixture:johndoe');
    cy.route('/api/projects*', 'fixture:johndoe/projects');
    cy.route('/api/activities*', 'fixture:johndoe/activities');
  });

  it('should persist on save', () => {
    // given
    cy.route('PUT', '/api/user', 'fixture:johndoe2');
    cy.route('/api/johndoe2', 'fixture:johndoe2');

    // when
    page.navigateTo();
    page.getNameField().type('{selectall}John Doe2');
    page.getUsernameField().type('{selectall}johndoe2');
    page.getSaveButton().click();

    // then
    cy.url().should('contain', 'johndoe2/settings');
  });

  it('should persist with 32 chars name on save', () => {
    // given
    const name = `${'John Doe'.repeat(4)}`;
    cy.route('PUT', '/api/user', 'fixture:johndoe-name-32');

    // when
    page.navigateTo();
    page.getNameField().type(`{selectall}${name}`);
    page.getSaveButton().click();

    // then
    cy.url().should('contain', `johndoe/settings`);
  });

  it('should not persist when name not unique', () => {
    // given
    cy.route({
      method: 'PUT',
      url: '/api/user',
      status: 400,
      response: 'fixture:johndoe-not-unique'
    });

    // when
    page.navigateTo();
    page.getNameField().type('{selectall}janesmith');

    // then
    page.getSaveButton().click();

    page.getNameFieldError().should('be.visible');
    cy.url().should('contain', 'johndoe/settings');
  });

  it('should not persist if username does not match pattern', () => {
    // given

    // when
    page.navigateTo();
    page.getUsernameField()
      .type('{selectall}john doe')
      .blur();

    // then
    page.getSaveButton().should('be.disabled');

    page.getUsernameFieldError().should('be.visible');
  });

  it('should not persist if name is too long', () => {
    // given

    // when
    page.navigateTo();
    page.getNameField().type(`{selectall}${'John Doe'.repeat(4)}J`);

    // then
    page.getSaveButton().should('be.disabled');
  });

  it('should not persist if username is too long', () => {
    // given
    const name = `${'johndoe'.repeat(4)}johnd`;

    // when
    page.navigateTo();

    // then
    page.getUsernameField().type(`{selectall}${name}`);
    page.getSaveButton().should('be.disabled');
  });
});
