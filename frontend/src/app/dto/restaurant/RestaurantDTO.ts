export interface RestaurantDTO {
    restaurantId: number;
    name: string;
    address: string;
    phone: string;
    description?: string;
    imageUrl?: string;
    status: 'OPEN' | 'CLOSED';
}

export interface RestaurantUpdateDTO {
    name?: string;
    address?: string;
    phone?: string;
    description?: string;
    status?: 'OPEN' | 'CLOSED';
}

export interface RestaurantCreateDTO {
    name: string;
    address: string;
    phone: string;
    description?: string;
}