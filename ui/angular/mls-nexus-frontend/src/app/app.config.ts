import { ApplicationConfig, provideZoneChangeDetection, provideZonelessChangeDetection, importProvidersFrom } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    // 1. DROP the legacy zone checker line: provideZoneChangeDetection({ eventCoalescing: true })
    // 2. SWAP IN the modern high-performance zoneless provider engine matching your Signals workflow:
    provideZonelessChangeDetection(),
    
    provideRouter(routes),
    provideHttpClient(),
    importProvidersFrom(FormsModule, ReactiveFormsModule)
  ]
};
