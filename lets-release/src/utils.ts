import {cli} from 'cli-ux';

export const run = async <T>(
  name: string,
  action: () => Promise<T>
): Promise<T> => {
  cli.action.start(name);
  const result = await action();
  cli.action.stop();
  return result;
};
