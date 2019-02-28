import {Injectable} from '@angular/core';
import {Observable} from "rxjs";
import {Project} from "../shared/project";
import {HttpClient, HttpParams} from "@angular/common/http";
import {map} from "rxjs/operators";

@Injectable({
  providedIn: 'root'
})
export class ProjectService {

  constructor(private readonly http: HttpClient) {
  }

  getProject(id: string, options?: {
    params?: HttpParams | {
      [param: string]: string | string[];
    }
  }): Observable<Project | undefined> {
    return this.http
      .get<Project>(`/api/project/${id}`, options)
      .pipe(
        map((project: Project) => ({
          ...project,
          whenCreated: new Date(project.whenCreated),
          whenUpdated: new Date(project.whenUpdated)
        }))
      );
  }
}
