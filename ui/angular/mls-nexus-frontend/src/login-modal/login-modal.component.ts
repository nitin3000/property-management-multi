import { Component, inject, signal, output } from '@angular/core';
import { AuthService } from '../app/core/services/auth.service';
import { FormsModule } from '@angular/forms'; // 🚨 1. ADD THIS IMPORT LINE
import { ConsoleLogger } from 'aws-amplify/utils';

@Component({
  selector: 'app-login-modal',
  templateUrl: './login-modal.component.html',
  imports: [FormsModule]
})
export class LoginModalComponent {
  private authService = inject(AuthService);

  closeModal = output<void>(); 

  // Form input bindings
  email = '';
  password = '';
  newPassword = '';

  // UI state management using Signals
  showChallengeScreen = signal(false);
  errorMessage = signal('');

  async onLoginSubmit() {
    this.errorMessage.set('');
    try {
      console.log('Sending credentials to AuthService...', this.email);

      const result = await this.authService.login(this.email, this.password);

      console.log('Cognito raw login result:', result);

      if (result.requiresNewPassword) {
              console.log('Switching UI flag showChallengeScreen to TRUE');
        // Switch the HTML view to collect the new password
        this.showChallengeScreen.set(true);
      } else if (result.success) {
        console.log('Successfully authenticated!');
        // Close modal logic goes here
      }
    } catch (err: any) {
      this.errorMessage.set(err.message || 'Authentication failed');
    }
  }

  async onChallengeSubmit() {
    this.errorMessage.set('');
    try {
      const isComplete = await this.authService.resolveNewPasswordChallenge(this.newPassword);
      if (isComplete) {
        console.log('Challenge cleared, token generated!');
        // Close modal logic goes here
      }
    } catch (err: any) {
      this.errorMessage.set(err.message || 'Failed to update password');
    }
  }
  onCloseClick() {
    this.closeModal.emit(); // 👈 Tells the header to set isModalOpen to false
  }

    async handleForcePasswordChange() {
    try {
      this.errorMessage.set('');
      // Confirm the sign in with the new password
      console.log(this.newPassword);
      const isSuccess = await this.authService.resolveNewPasswordChallenge(this.newPassword);
      
      // Success! Logic to redirect or close modal
      this.closeModal.emit();
    } catch (error: any) {
      this.errorMessage = error.message || 'Failed to update password';
    }
  }
}
