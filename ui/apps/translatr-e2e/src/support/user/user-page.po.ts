import { Page } from '../page.po';

export class UserPage extends Page {
  constructor(private readonly username: string) {
    super();
  }

  navigateTo(): UserPage {
    cy.visit(`/${this.username}`);
    return this;
  }
}
