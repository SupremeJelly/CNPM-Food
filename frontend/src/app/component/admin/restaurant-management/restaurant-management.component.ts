import { Component, OnInit } from '@angular/core';
import { RestaurantService } from '../../../service/restaurant.service';
import { Page } from '../../../dto/Page';
import { Restaurant } from '../../../dto/Restaurant';

@Component({
  selector: 'app-restaurant-management',
  templateUrl: './restaurant-management.component.html',
  styleUrl: './restaurant-management.component.css'
})
export class RestaurantManagementComponent implements OnInit {
  restaurants: Restaurant[] = [];
  isLoading: boolean = true;
  errorMessage: string | null = null;
  defaultImage = 'https://via.placeholder.com/400x300/e2e8f0/475569?text=Restaurant';

  constructor(private restaurantService: RestaurantService) {}

  ngOnInit(): void {
    this.loadRestaurants();
  }

  loadRestaurants(): void {
    this.isLoading = true;
    this.errorMessage = null;

    this.restaurantService.getAllRestaurants(0, 10).subscribe({
      next: (data: Page<Restaurant>) => {
        this.restaurants = data.content;
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = 'Failed to load restaurants';
        this.isLoading = false;
        console.error('Error loading restaurants:', error);
      }
    });
  }

  viewDetails(restaurantId: number): void {
    console.log('View restaurant details:', restaurantId);
    // TODO: Navigate to restaurant details page
  }

  editRestaurant(restaurantId: number): void {
    console.log('Edit restaurant:', restaurantId);
    // TODO: Open edit modal or navigate to edit page
    alert('Edit functionality will be implemented soon');
  }

  deleteRestaurant(restaurantId: number): void {
    if (confirm('Are you sure you want to delete this restaurant?')) {
      console.log('Delete restaurant:', restaurantId);
      // TODO: Call delete API
      alert('Delete functionality will be implemented soon');
    }
  }
}
