import Command, { flags } from "@oclif/command";
import { ReleaseType } from "semver";
import { ChangelogService } from "./services/changelog.service";
import { FileService } from "./services/file.service";
import { ReleaseConfig } from "./release.config";
import { ReleaseFactory } from "./release.factory";
import { GitService } from "./services/git.service";

class LetsReleaseCommand extends Command {
  static description =
    "Generate data (users, projects, locales, keys) by using the API";

  static flags = {
    version: flags.version({ char: "v" }),
    help: flags.help({ char: "h" }),

    "main-branch": flags.string({
      char: "m",
      description: "The main branch",
      default: "main"
    }),

    "prod-branch": flags.string({
      char: "p",
      description: "The production branch",
      default: "production"
    }),

    "github-token": flags.string({
      char: "t",
      description: "The Github token to use when generating the changelog"
    }),

    "dry-run": flags.boolean({
      description: "only update the version, do not commit anything"
    })
  };

  static args = [
    {
      name: "releaseType",
      description: "The release type to increment the version by.",
      required: true
    }
  ];

  async run() {
    const command = this.parse(LetsReleaseCommand);

    const fileService = new FileService();
    const gitService = new GitService();
    const changelogService = new ChangelogService();
    const releaseFactory = new ReleaseFactory(
      gitService,
      fileService,
      changelogService
    );

    const type =
      command.args.releaseType === "release"
        ? "patch"
        : (command.args.releaseType as ReleaseType);
    const version = await fileService.readAndIncrementVersion(type);
    const config: ReleaseConfig = {
      mainBranch: command.flags["main-branch"],
      productionBranch: command.flags["prod-branch"],
      tag: `v${version.raw}`,
      releaseBranch: `release/v${version.major}.${version.minor}.x`,
      githubToken: command.flags["github-token"]
    };

    const release = releaseFactory.create(version, config);

    if (command.flags["dry-run"]) {
      console.log("Dry run activated, will not commit");
      return release.updateVersion(version);
    }

    await release.validate(version);

    await release.updateVersion(version);

    await release.release(version);
  }
}

export = LetsReleaseCommand;
