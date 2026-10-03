// src/app/core/interceptors/api.interceptor.ts
import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const apiInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getBearerToken();

  // Intercept requests targeting your specific API Gateway subdomain
  if (req.url.includes('://rephance.com')) {
    let headers = req.headers
      .set('X-Content-Type-Options', 'nosniff')
      .set('X-Frame-Options', 'DENY');

    // Dynamically inject the JWT token if the user is authenticated
    if (token) {
      headers = headers.set('Authorization', `Bearer ${token}`);
    }

    const secureReq = req.clone({ headers });
    return next(secureReq);
  }

  return next(req);
};
