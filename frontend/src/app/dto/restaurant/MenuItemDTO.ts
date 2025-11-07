export interface MenuItemDTO {
    menuItemId: number;
    restaurantId: number;
    name: string;
    description?: string;
    price: number;
    imageUrl?: string;
    category: string;
    available: boolean;
    stock?: number; // optional stock to match frontend MenuItem shape when needed
}

export interface MenuItemUpdateDTO {
    name?: string;
    description?: string;
    price?: number;
    category?: string;
    available?: boolean;
}

export interface MenuItemCreateDTO {
    restaurantId: number;
    name: string;
    description?: string;
    price: number;
    category: string;
}