import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-portlet-home',
  templateUrl: './portlet-home.component.html',
  styleUrls: ['./portlet-home.component.scss']
})
export class PortletHomeComponent {
  constructor(private router: Router) {}

  navigateTo(path: string): void {
    this.router.navigate([path]);
  }
}