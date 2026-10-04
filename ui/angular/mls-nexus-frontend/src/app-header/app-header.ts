import { Component, OnInit, signal, computed, inject, Output } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../app/core/services/auth.service';
import { FormsModule } from '@angular/forms'; // 🚨 1. ADD THIS IMPORT LINE
import { LoginModalComponent } from '../login-modal/login-modal.component';


@Component({
  selector: 'app-header',
  standalone: true,
  imports: [FormsModule, LoginModalComponent],
  templateUrl: './app-header.component.html'
  //,styleUrls: ['./app-header.component.scss']
})
export class AppHeaderComponent {
  private authService = inject(AuthService);
  private router = inject(Router);
  readonly isModalOpen = signal<boolean>(false);

    // 3. Local input bindings tracking form inputs


  ngOnInit(): void {
    // Execute a safe initial fetch to verify connection lines immediately
  }

triggerLoginModal() {
  // Toggle a local flag instead of forcing a route change
  this.isModalOpen.set(true); 
}

}