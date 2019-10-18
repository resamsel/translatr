import { Page } from './page.po';

export class ProjectSettingsPage extends Page {
  navigateTo(): ProjectSettingsPage {
    cy.visit('/johndoe/p1/settings');
    return this;
  }
}

