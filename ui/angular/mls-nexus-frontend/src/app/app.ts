import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpClient, HttpParams } from '@angular/common/http';
import { PropertyListing } from './models/property_listing';
import { SpringPageResponse } from './models/spring_page_response';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, CurrencyPipe, DatePipe],
  templateUrl: './app.html'
})
export class AppComponent implements OnInit {
  private http = inject(HttpClient);
  private fb = inject(FormBuilder);

  // Replace the old endpoint path with this exact string match:
  private readonly API_URL = environment.apiUrl;


  // State Management Signals Matrices
  allListings = signal<PropertyListing[]>([]);
  currentPage = signal<number>(1);
  pageSize = signal<number>(4);
  isLoading = signal<boolean>(false);
  errorMessage = signal<string>('');
  selectedProperty = signal<PropertyListing | null>(null);

  // CRITICAL FIX: Instantiate the form controls immediately so the template never encounters an undefined object loop error
  searchForm: FormGroup = this.fb.group({
    keyword: [''],
    city: [''],
    minPrice: [null, [Validators.min(0)]],
    maxPrice: [null, [Validators.min(0)]],
    minBedrooms: [null, [Validators.min(0)]],
    targetBudget: [null, [Validators.min(0)]]
  }, {
    validators: (group) => {
      const min = group.get('minPrice')?.value;
      const max = group.get('maxPrice')?.value;
      return (min !== null && max !== null && min > max) ? { invalidRange: true } : null;
    }
  });

  // Safe slice window mapping calculations
  paginatedListings = computed(() => {
    const listings = this.allListings();
    const start = (this.currentPage() - 1) * this.pageSize();
    return listings.slice(start, start + this.pageSize());
  });

  totalPages = computed(() => {
    return Math.ceil(this.allListings().length / this.pageSize()) || 1;
  });

  ngOnInit(): void {
    // Execute a safe initial fetch to verify connection lines immediately
    this.fetchBackendListings({});
  }

  onApplyFilters(): void {
    if (this.searchForm.invalid) {
      this.errorMessage.set('Invalid validation boundaries. Verify that Max Price is greater than Min Price.');
      return;
    }
    this.errorMessage.set('');
    this.currentPage.set(1);
    this.fetchBackendListings(this.searchForm.value);
  }

  private fetchBackendListings(filters: any): void {
    this.isLoading.set(true);
    let params = new HttpParams();
    
    if (filters.keyword) params = params.set('keyword', filters.keyword);
    if (filters.city) params = params.set('city', filters.city);
    if (filters.minPrice) params = params.set('minPrice', filters.minPrice.toString());
    if (filters.maxPrice) params = params.set('maxPrice', filters.maxPrice.toString());
    if (filters.minBedrooms) params = params.set('minBedrooms', filters.minBedrooms.toString());
    if (filters.targetBudget) params = params.set('targetBudget', filters.targetBudget.toString());

    // CRITICAL FIX: Change the generic type token array target to SpringPageResponse
    this.http.get<SpringPageResponse>(this.API_URL, { params }).subscribe({
      next: (response: SpringPageResponse) => {
        // Extract the target property data array hidden inside the spring object content block
        this.allListings.set(response.content || []); 
        this.isLoading.set(false);
      },
      error: (err) => {
        console.warn('Spring Boot connection fallback activated.', err);
        this.errorMessage.set('Displaying connection backup fallback listings.');
        this.allListings.set([]);
        this.isLoading.set(false);
      }
    });
  }

  clearFilters(): void {
    this.searchForm.reset();
    this.errorMessage.set('');
    this.fetchBackendListings({});
  }

  selectProperty(property: PropertyListing): void {
    this.selectedProperty.set(property);
  }
}
