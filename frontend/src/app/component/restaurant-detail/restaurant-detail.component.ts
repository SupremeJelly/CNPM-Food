import { Component, Input, OnInit } from '@angular/core';
import { MenuItem } from '../../dto/MenuItem';
import { MenuItemDTO } from '../../dto/restaurant/MenuItemDTO';
import { RestaurantService } from '../../service/restaurant.service';
import { ActivatedRoute } from '@angular/router';
import { CartItem } from '../../dto/CartItem';
import { CartService } from '../../service/cart.service';

@Component({
  selector: 'app-restaurant-detail',
  templateUrl: './restaurant-detail.component.html',
  styleUrl: './restaurant-detail.component.css'
})
export class RestaurantDetailComponent implements OnInit {

  menuItems: MenuItemDTO[] = [];
  restaurantId!: number;

  constructor(
    private restaurantService: RestaurantService, 
    private route: ActivatedRoute,
    private cartService : CartService) { }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.restaurantId = params['id']; 
      this.loadMenuItems(this.restaurantId);
    });
  }

  loadMenuItems(restaurantId: number): void {
    this.restaurantService.getMenuItemsByRestaurantId(restaurantId)
    .subscribe((response: MenuItemDTO[]) => {
      this.menuItems = response;
    });
  }

  // addToCart(menuItem: MenuItem) {
  //   const cart = localStorage.getItem('cart');
  //   const cartItems: CartItem[] = cart ? JSON.parse(cart) : [];
    
  //   const existingItem = cartItems.find(cartItem => cartItem.menuItem.menuItemId === menuItem.menuItemId);
  //   if (existingItem) {
  //       existingItem.quantity++; // Increase quantity by 1
  //   } else {
  //       cartItems.push({ menuItem: menuItem, quantity: 1 }); // Add new item with quantity 1
  //   }

  //   localStorage.setItem('cart', JSON.stringify(cartItems));
  // }

  addToCart(menuItemDto: MenuItemDTO) {
    // Map MenuItemDTO -> MenuItem shape expected by CartItem
    const menuItem: MenuItem = {
      menuItemId: menuItemDto.menuItemId,
      name: menuItemDto.name,
      price: menuItemDto.price,
      restaurantId: menuItemDto.restaurantId,
      stock: menuItemDto.stock ?? 0,
      imageUrl: menuItemDto.imageUrl
    };

    const cartItem: CartItem = {
      menuItem: menuItem,
      quantity: 1
    };

    this.cartService.addToCart(cartItem);
  }
}
