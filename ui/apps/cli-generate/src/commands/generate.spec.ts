import { GenerateCommand } from './generate';

describe('GenerateCommand', () => {
  it('should create', () => {
    // given
    const argv = [];
    const config = undefined;

    // when
    const actual = new GenerateCommand(argv, config);

    // then
    expect(actual).toBeDefined();
  });
});
