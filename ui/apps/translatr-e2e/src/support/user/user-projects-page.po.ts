import { Page } from '../page.po';

export class UserProjectsPage extends Page {
  constructor(private readonly username: string) {
    super();
  }

  navigateTo(): UserProjectsPage {
    cy.visit(`/${this.username}/projects`);
    return this;
  }
}
