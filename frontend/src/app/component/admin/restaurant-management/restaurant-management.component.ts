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
}
