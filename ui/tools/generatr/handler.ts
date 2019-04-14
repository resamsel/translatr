import { Injector } from "@angular/core";
import { switchMap } from "rxjs/operators";
import { Action, State } from "./state";
import { Observable, of } from "rxjs";
import { createRandomUser, deleteRandomUser, me, updateRandomUser } from "./user";
import { UserService } from "../../libs/translatr-sdk/src/lib/services/user.service";

export const handleCommand = (injector: Injector) => switchMap((state: State): Observable<State> => {
  switch (state.type) {
    case Action.ShowConfig:
      return of({...state, message: `Config: ${JSON.stringify(state.config)}`});
    case Action.Me:
      return me(injector.get(UserService), state);
    case Action.CreateRandomUser:
      return createRandomUser(injector.get(UserService), state);
    case Action.DeleteRandomUser:
      return deleteRandomUser(injector.get(UserService), state);
    case Action.UpdateRandomUser:
      return updateRandomUser(injector.get(UserService), state);
    default:
      return of({...state, message: `Unknown command ${state.type}`});
  }
});

