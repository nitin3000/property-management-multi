export interface PropertyListing {
  id: string;
  source: string;
  address: string;
  city: string;
  state: string; // Ensure this is mapped
  price: number;
  bedrooms: number;
  bathrooms: number; // Backed by your Float fix
  sqft?: number;
  latitude: number;  // Backed by your Float fix
  longitude: number; // Backed by your Float fix
  listedDate: string;
  status?: string;
  description?: string;
  relevanceScore?: number;
}