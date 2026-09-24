// Add this interface directly underneath your existing PropertyListing interface definition:
import { PropertyListing } from './property_listing';

export interface SpringPageResponse {
  content: PropertyListing[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
