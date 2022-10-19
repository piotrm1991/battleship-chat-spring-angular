import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { StorageService } from '../_services/storage.service';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {
  currentUser: any;

  constructor(private storageService: StorageService,
              private router: Router) { 
                if (!storageService.isLoggedIn()) {
                  router.navigate(['']);
                }
              }

  ngOnInit(): void {
    this.currentUser = this.storageService.getUser();
  }
}