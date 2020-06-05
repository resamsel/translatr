import { Page } from '../page.po';

export class ProjectMembersPage extends Page {
  constructor(private readonly username: string, private readonly projectName: string) {
    super();
  }

  navigateTo(): ProjectMembersPage {
    cy.visit(`/${this.username}/${this.projectName}/members`);
    return this;
  }
}
