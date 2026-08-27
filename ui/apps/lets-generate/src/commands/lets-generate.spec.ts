import { LetsGenerateCommand } from './lets-generate';

describe('LoadGenerate', () => {
  it('should create', async () => {
    // given
    const argv = ['-p', 'John'];

    // when
    const actual = await LetsGenerateCommand.run(argv).then(
      _result => 'result',
      _reason => 'error'
    );

    // then
    expect(actual).toBe('error');
  });
});
