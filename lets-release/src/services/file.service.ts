import {promises} from 'fs';
import {inc, parse, ReleaseType, SemVer} from 'semver';

export class FileService {
  readJson(filename: string): Promise<any> {
    return promises.readFile(filename, {encoding: 'utf8'}).then(data => JSON.parse(data));
  }

  readAndIncrementVersion(releaseType: ReleaseType): Promise<SemVer> {
    return this.readJson('package.json')
      .then(json => ({
        current: json.version,
        incremented: inc(json.version, releaseType),
      }))
      .then(({current, incremented}) => {
        if (incremented === null) {
          throw new Error(`Cannot increment version ${current} (${releaseType})`);
        }

        console.log(`Version ${current} -> ${incremented} (${releaseType})`);
        console.log();

        const version = parse(incremented);

        if (version === null) {
          throw new Error(`Could not parse version ${incremented}`);
        }

        return version;
      });
  }

  updateJson(filename: string, version: string): Promise<void> {
    return this.readJson(filename)
      .then(json => ({...json, version}))
      .then(json => JSON.stringify(json, null, 2))
      .then(s => promises.writeFile(filename, s + '\n'));
  }

  updateFile(filename: string, searchValue: RegExp, replaceValue: string): Promise<void> {
    return promises
      .readFile(filename, {encoding: 'utf8'})
      .then(data => data.replace(searchValue, replaceValue))
      .then(s => promises.writeFile(filename, s));
  }

  updateYaml(filename: string, version: string): Promise<void> {
    return promises
      .readFile(filename, {encoding: 'utf8'})
      .then(data => data.replace(/resamsel\/translatr:.*/, `resamsel/translatr:${version}`))
      .then(data =>
        data.replace(
          /resamsel\/translatr-loadgenerator:.*/,
          `resamsel/translatr-loadgenerator:${version}`
        )
      )
      .then(s => promises.writeFile(filename, s));
  }
}
