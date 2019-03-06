import {Injectable} from '@angular/core';
import {Observable} from "rxjs";
import {PagedList} from "../shared/paged-list";
import {Activity} from "../shared/activity";
import {HttpClient} from "@angular/common/http";

export interface ActivityCriteria {
  userId?: string;
  projectId?: string;
  limit?: number;
  offset?: number;
  order?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ActivityService {

  constructor(private readonly http: HttpClient) {
  }

  activityList(criteria: ActivityCriteria): Observable<PagedList<Activity>> {
    return this.http.get<PagedList<Activity>>(
      '/api/activities',
      {params: criteria as { [param: string]: string | string[]; }}
    );
  }
}
