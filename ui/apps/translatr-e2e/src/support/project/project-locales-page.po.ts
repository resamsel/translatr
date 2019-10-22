import { Page } from '../page.po';

export class ProjectLocalesPage extends Page {
  constructor(
    private readonly username: string,
    private readonly projectName: string
  ) {
    super();
  }

  navigateTo(): ProjectLocalesPage {
    cy.visit(`/${this.username}/${this.projectName}/locales`);
    return this;
  }
}
