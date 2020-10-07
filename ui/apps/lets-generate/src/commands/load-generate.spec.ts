import { LoadGenerateCommand } from './load-generate';

describe('LoadGenerate', () => {
  it('should create', async () => {
    // given
    const argv = ['-p', 'John'];

    // when
    const actual = await LoadGenerateCommand.run(argv).then(
      result => 'result',
      reason => 'error'
    );

    // then
    expect(actual).toBe('error');
  });
});
