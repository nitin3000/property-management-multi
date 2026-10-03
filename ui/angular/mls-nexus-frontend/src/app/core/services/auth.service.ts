// src/app/core/services/auth.service.ts
import { inject, Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap, catchError, of } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private authUrl = 'https://rephance.com';

  // 1. Reactive Signals for core state
  // Initializes by checking localStorage so the user stays logged in on page refresh
  private _token = signal<string | null>(localStorage.getItem('auth_token'));
  private _username = signal<string | null>(localStorage.getItem('user_name'));

  // 2. Computed signals expose read-only state to components
  readonly token = this._token.asReadonly();
  readonly username = this._username.asReadonly();
  readonly isLoggedIn = computed(() => !!this._token());

  login(credentials: { email: string; pass: string }) {
    return this.http.post<{ token: string; username: string }>(this.authUrl, credentials).pipe(
      tap(res => {
        // Save to storage and update signals reactively
        localStorage.setItem('auth_token', res.token);
        localStorage.setItem('user_name', res.username);
        this._token.set(res.token);
        this._username.set(res.username);
      })
    );
  }

  logout() {
    localStorage.removeItem('auth_token');
    localStorage.removeItem('user_name');
    this._token.set(null);
    this._username.set(null);
  }
}
