import Command, { flags } from '@oclif/command';
import { ReleaseType } from 'semver';
import { ChangelogService } from './services/changelog.service';
import { FileService } from './services/file.service';
import { ReleaseConfig } from './release.config';
import { ReleaseFactory } from './release.factory';
import { GitService } from './services/git.service';

import 'dotenv/config';

class LetsReleaseCommand extends Command {
  static description = 'Generate data (users, projects, locales, keys) by using the API';

  static flags = {
    version: flags.version({ char: 'v' }),
    help: flags.help({ char: 'h' }),

    config: flags.string({
      char: 'c',
      description: 'The config JSON to read version updates from',
      default: 'release.json'
    }),

    'version-file': flags.string({
      char: 'V',
      description: 'The JSON file where the current version is read from ($.version)',
      default: 'package.json'
    }),

    'main-branch': flags.string({
      char: 'm',
      description: 'The main branch',
      default: 'main'
    }),

    'prod-branch': flags.string({
      char: 'p',
      description: 'The production branch',
      default: 'production'
    }),

    'github-token': flags.string({
      char: 't',
      description: 'The Github token to use when generating the changelog'
    }),

    'tag-pre-release': flags.boolean({
      description: 'whether or not put a tag on a pre-release version (default off)'
    }),

    'dry-run': flags.boolean({
      description: 'only update the version, do not commit anything'
    })
  };

  static args = [
    {
      name: 'releaseType',
      description: 'The release type to increment the version by.',
      required: true
    }
  ];

  async run() {
    const command = this.parse(LetsReleaseCommand);

    const fileService = new FileService();
    const gitService = new GitService();
    const changelogService = new ChangelogService();
    const releaseFactory = new ReleaseFactory(gitService, fileService, changelogService);

    const type =
      command.args.releaseType === 'release' ? 'patch' : (command.args.releaseType as ReleaseType);
    const version = await fileService.readAndIncrementVersion(command.flags['version-file'], type);
    const defaultConfig = await fileService.readJson(command.flags.config);
    const config: ReleaseConfig = {
      ...defaultConfig,
      mainBranch: command.flags['main-branch'],
      productionBranch: command.flags['prod-branch'],
      tag: `v${version.raw}`,
      releaseBranch: `release/v${version.major}.${version.minor}.x`,
      githubToken: command.flags['github-token'],
      tagPreRelease: command.flags['tag-pre-release']
    };

    const release = releaseFactory.create(version, config);

    if (command.flags['dry-run']) {
      console.log('Dry run activated, will not commit');
      return release.updateVersion(version);
    }

    await release.validate(version);

    await release.updateVersion(version);

    await release.release(version);
  }
}

export = LetsReleaseCommand;
