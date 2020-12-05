export class ReleaseError extends Error {
  constructor(public readonly messages: string[] = []) {
    super(messages.map(message => `\n - ${message}`).join(''));
  }
}
