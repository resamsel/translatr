import {RequestCriteria} from "./request-criteria";

export interface ProjectCriteria extends RequestCriteria {
  owner?: string;
}

