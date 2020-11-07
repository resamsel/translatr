import { LetsGenerateCommand } from './lets-generate';

describe('LoadGenerate', () => {
  it('should create', async () => {
    // given
    const argv = ['-p', 'John'];

    // when
    const actual = await LetsGenerateCommand.run(argv).then(
      result => 'result',
      reason => 'error'
    );

    // then
    expect(actual).toBe('error');
  });
});
