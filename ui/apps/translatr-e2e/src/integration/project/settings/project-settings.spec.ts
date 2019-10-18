import { ProjectSettingsPage } from '../../../support/project-settings.po';

describe('Translatr Project Settings', () => {
  let page: ProjectSettingsPage;

  beforeEach(() => {
    page = new ProjectSettingsPage();

    cy.clearCookies();
    cy.server();

    cy.route('/api/me', 'fixture:me');
  });

  it('should have page name Project Settings', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getPageName()
      .should('have.text', 'Project Settings');
  });
});
