import {interval, merge, Observable, of} from "rxjs";
import {mapTo, scan} from "rxjs/operators";
import {Action, Config, State} from "./state";
import {envAsNumber, envAsString} from "./utils";
import {handleCommand} from "./handler";
import {Injector} from "@angular/core";
import {createInjector} from "./api";

const config: Config = {
  baseUrl: envAsString('ENDPOINT', 'http://localhost:9000'),
  accessToken: envAsString('ACCESS_TOKEN', ''),
  intervals: {
    // every three minutes
    createUser: envAsNumber('CREATE_USER_INTERVAL', 3 * 60 * 1000),
    // every minute
    updateUser: envAsNumber('UPDATE_USER_INTERVAL', 60 * 1000),
    // every 15 minutes
    deleteUser: envAsNumber('DELETE_USER_INTERVAL', 15 * 60 * 1000),

    // every 5 minutes
    createProject: envAsNumber('CREATE_PROJECT_INTERVAL', 5 * 60 * 1000),
    // every minute
    updateProject: envAsNumber('UPDATE_PROJECT_INTERVAL', 60 * 1000),
    // every hour
    deleteProject: envAsNumber('DELETE_PROJECT_INTERVAL', 60 * 60 * 1000)
  }
};

// const platform = platformBrowserDynamic();
// const injector = platform.injector;
// platform.bootstrapModule(TranslatrSdkModule)
//   .catch(console.error);

const injector: Injector = createInjector(config.baseUrl, config.accessToken);
const commands$: Observable<Partial<State>> = merge(
  of({type: Action.ShowConfig}),
  of({type: Action.Me}),
  interval(config.intervals.createUser).pipe(mapTo({type: Action.CreateRandomUser})),
  interval(config.intervals.updateUser).pipe(mapTo({type: Action.UpdateRandomUser})),
  interval(config.intervals.deleteUser).pipe(mapTo({type: Action.DeleteRandomUser})),

  interval(config.intervals.createProject).pipe(mapTo({type: Action.CreateRandomProject})),
  interval(config.intervals.updateProject).pipe(mapTo({type: Action.UpdateRandomProject})),
  interval(config.intervals.deleteProject).pipe(mapTo({type: Action.DeleteRandomProject}))
);

commands$.pipe(
  scan((acc: State, next: State) => ({...acc, ...next}), {config}),
  handleCommand(injector)
).subscribe(
  (state: State) => console.log(`${new Date().toISOString()}: ${state.message}`),
  (state: State) => console.error(`${new Date().toISOString()}: ${state.message}`)
);
