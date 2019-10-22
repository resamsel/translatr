import { Page } from '../page.po';

export class ProjectKeysPage extends Page {
  constructor(
    private readonly username: string,
    private readonly projectName: string
  ) {
    super();
  }

  navigateTo(): ProjectKeysPage {
    cy.visit(`/${this.username}/${this.projectName}/keys`);
    return this;
  }
}
