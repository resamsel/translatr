export class ReleaseError extends Error {
  constructor(public readonly messages: string[] = []) {
    super(messages.map(message => `\n - ${message}`).join(''));
  }

  withMessages(messages: string[]): ReleaseError {
    return new ReleaseError([...this.messages, ...messages]);
  }
}
