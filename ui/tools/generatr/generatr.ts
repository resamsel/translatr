import {interval, merge, Observable, of, Subject} from "rxjs";
import {exhaustMap, mapTo, scan, startWith, tap, withLatestFrom} from "rxjs/operators";
import {Action, Command, Config, State} from "./state";
import {envAsNumber, envAsString} from "./utils";
import {handleCommand} from "./handler";
import {Injector} from "@angular/core";
import {createInjector} from "./api";

const config: Config = {
  baseUrl: envAsString('ENDPOINT', 'http://localhost:9000'),
  accessToken: envAsString('ACCESS_TOKEN', ''),
  intervals: {
    stressFactor: envAsNumber('STRESS_FACTOR', 1),

    // every minute
    me: envAsNumber('ME_INTERVAL', 10 * 60 * 1000),

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

const intervals = config.intervals;
const injector: Injector = createInjector(config.baseUrl, config.accessToken);
const stateCommand$ = new Subject<Partial<State>>();

const state$: Observable<State> = stateCommand$.asObservable().pipe(
  startWith({config}),
  scan((acc: State, next: State) => ({...acc, ...next}))
);

const commands$: Observable<Command> = merge(
  of({type: Action.ShowConfig}),

  // interval(intervals.me / intervals.stressFactor).pipe(mapTo({type: Action.Me})),
  // interval(intervals.createUser / intervals.stressFactor).pipe(mapTo({type: Action.CreateRandomUser})),
  // interval(intervals.updateUser / intervals.stressFactor).pipe(mapTo({type: Action.UpdateRandomUser})),
  // interval(intervals.deleteUser / intervals.stressFactor).pipe(mapTo({type: Action.DeleteRandomUser})),
  //
  // interval(intervals.createProject / intervals.stressFactor).pipe(mapTo({type: Action.CreateRandomProject})),
  interval(intervals.updateProject / intervals.stressFactor).pipe(mapTo({type: Action.UpdateRandomProject})),
  // interval(intervals.deleteProject / intervals.stressFactor).pipe(mapTo({type: Action.DeleteRandomProject}))
);

commands$.pipe(
  withLatestFrom(state$),
  handleCommand(injector),
//  tap((state: State) => stateCommand$.next(state))
).subscribe(
  (state: State) => console.log(`${new Date().toISOString()}: ${state.message}`),
  (state: State) => console.error(`${new Date().toISOString()}: ${state.message}`)
);
