// src/app/services/auth.service.ts
import { Injectable, signal, computed } from '@angular/core';
import { signIn, signOut, getCurrentUser, fetchAuthSession, confirmSignIn } from 'aws-amplify/auth';
import { Amplify } from 'aws-amplify';

export interface LoginResult {
  success: boolean;
  requiresNewPassword: boolean;
}

@Injectable({
  providedIn: 'root' // Standard root singleton, matching Spring's @Service
})
export class AuthService {
  // 1. Reactive Signal States for user info and loading status
  readonly currentUser = signal<any | null>(null);
  readonly isLoading = signal<boolean>(false);
  readonly authError = signal<string | null>(null);

  // 2. Computed Read-Only state to check if authenticated (Updates automatically)
  readonly isAuthenticated = computed(() => this.currentUser() !== null);

  constructor() {
    // Automatically check for an active user session on app load
    this.checkCurrentUser();
    // 2. Initialize your Cognito Pool variables
    Amplify.configure({
      Auth: {
        Cognito: {
          userPoolId: 'us-east-1_SFRUDVD13',       // Your AWS User Pool ID
          userPoolClientId: '495gfgv0rq7ia74c6cdjt6tvlp'   // Your AWS App Client ID
        }
      }
    });
  }

  /**
   * Main Login Method using AWS Cognito
   */
  async login(username: string, password: string): Promise<LoginResult> {
    this.isLoading.set(true);
    this.authError.set(null);

    try {
      const { isSignedIn, nextStep } = await signIn({ username, password });
      
      // Handle advanced banking scenarios (e.g., Force Change Password / MFA)
      if (nextStep.signInStep === 'CONFIRM_SIGN_IN_WITH_NEW_PASSWORD_REQUIRED') {
        return { success: false, requiresNewPassword: true };
      }

      if (isSignedIn) {
        await this.checkCurrentUser();
        return { success: true, requiresNewPassword: false };
      }
      
      return { success: false, requiresNewPassword: false };

    } catch (error: any) {
      this.authError.set(error.message || 'Authentication failed.');
      throw error;
    } finally {
      this.isLoading.set(false);
    }
  }

  /**
   * Main Logout Method
   */
  async logout(): Promise<void> {
    try {
      await signOut();
      this.currentUser.set(null);
    } catch (error) {
      console.error('Error signing out:', error);
    }
  }

  /**
   * Internal Helper to grab current user details from local storage cache
   */
  private async checkCurrentUser(): Promise<void> {
    try {
      const user = await getCurrentUser();
      this.currentUser.set(user);
    } catch {
      this.currentUser.set(null); // No active session found
    }
  }

  /**
   * Accessor method for the Angular HTTP Interceptor to grab the active JWT
   */
  async getBearerToken(): Promise<string | null> {
    try {
      const session = await fetchAuthSession();
      return session.tokens?.idToken?.toString() || null;
    } catch {
      return null;
    }
  }

  /**
   * Submits the new permanent password to clear the Cognito challenge [1]
   */
  async resolveNewPasswordChallenge(newPassword: string): Promise<boolean> {
    try {
      // confirmSignIn automatically links to the active in-flight challenge context [1]
      const { isSignedIn } = await confirmSignIn({ challengeResponse: newPassword });
      
      if (isSignedIn) {
        await this.getBearerToken();
      }
      return isSignedIn;
    } catch (error) {
      console.error('Error confirming new password challenge:', error);
      throw error;
    }
  }
}

