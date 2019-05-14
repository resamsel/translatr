import { Component, Inject, OnInit } from '@angular/core';
import { ENDPOINT_URL } from '@translatr/utils';

@Component({
  selector: 'dev-login-page',
  templateUrl: './login-page.component.html',
  styleUrls: ['./login-page.component.scss']
})
export class LoginPageComponent implements OnInit {

  constructor(@Inject(ENDPOINT_URL) public readonly endpointUrl: string) { }

  ngOnInit() {
  }

}
