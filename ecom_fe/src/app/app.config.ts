import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors, withFetch } from '@angular/common/http';

import { routes } from './app.routes';
import { provideClientHydration, withHttpTransferCacheOptions } from '@angular/platform-browser';
import { importProvidersFrom } from '@angular/core';
import { adminRoutes } from './components/admin/admin-routes';
import { RouterModule } from '@angular/router';
import { tokenInterceptor } from './interceptors/token.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes), 
    importProvidersFrom(RouterModule.forChild(adminRoutes)),    
    provideHttpClient(
      withInterceptors([tokenInterceptor]),
      withFetch()
    ),
    // SSR: cache GET requests để tránh double-fetch khi hydration
    provideClientHydration(
      withHttpTransferCacheOptions({
        includePostRequests: false
      })
    ),
  ]
};
