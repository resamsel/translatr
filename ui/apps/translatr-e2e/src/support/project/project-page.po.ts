import { Page } from '../page.po';
import { ProjectSettingsPage } from './project-settings-page.po';

export class ProjectPage extends Page {
  constructor(public readonly username: string, public readonly projectName: string) {
    super();
  }

  navigateTo(): ProjectPage {
    cy.visit(`/${this.username}/${this.projectName}`);
    return this;
  }

  navigateToSettings(): ProjectSettingsPage {
    return new ProjectSettingsPage(this.username, this.projectName).navigateTo();
  }

  getDescription(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('p.description');
  }
}
