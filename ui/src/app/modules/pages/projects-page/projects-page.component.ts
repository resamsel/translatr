import { Component, OnInit } from '@angular/core';
import {Observable} from "rxjs";
import {PagedList} from "../../../shared/paged-list";
import {Project} from "../../../shared/project";
import {HttpClient} from "@angular/common/http";

@Component({
  selector: 'app-projects-page',
  templateUrl: './projects-page.component.html',
  styleUrls: ['./projects-page.component.scss']
})
export class ProjectsPageComponent implements OnInit {
  projects$: Observable<PagedList<Project>>;

  constructor(private http: HttpClient) {
  }

  ngOnInit(): void {
    this.projects$ = this.http.get<PagedList<Project>>('/api/projects');
  }
}
