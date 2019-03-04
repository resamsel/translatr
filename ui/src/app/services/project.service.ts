import {Injectable} from '@angular/core';
import {Observable} from "rxjs";
import {Project} from "../shared/project";
import {HttpClient, HttpParams} from "@angular/common/http";
import {map} from "rxjs/operators";
import {PagedList} from "../shared/paged-list";

interface Temporal {
  whenCreated: Date | string;
  whenUpdated: Date | string
}

function convertTemporals<T extends Temporal>(t: T): T {
  return {
    ...(t as object),
    whenCreated: new Date(t.whenCreated),
    whenUpdated: new Date(t.whenUpdated)
  } as T;
}

@Injectable({
  providedIn: 'root'
})
export class ProjectService {

  constructor(private readonly http: HttpClient) {
  }

  getProjectByOwnerAndName(username: string, projectName: string, options?: {
    params?: HttpParams | {
      [param: string]: string | string[];
    }
  }): Observable<Project | undefined> {
    return this.http
      .get<Project>(`/api/${username}/${projectName}`, options)
      .pipe(
        map((project: Project) => ({
          ...convertTemporals(project),
          locales: project.locales.map(convertTemporals)
        }))
      );
  }

  getProjects(options?: {
    params?: HttpParams | {
      [param: string]: string | string[];
    }
  }): Observable<PagedList<Project>> {
    return this.http.get<PagedList<Project>>('/api/projects', options);
  }
}
