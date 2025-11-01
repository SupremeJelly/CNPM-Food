import { Component, OnInit } from '@angular/core';
import { UserService } from '../../../../app/service/user.service';
import { Page } from '../../../dto/Page';
import { UserDTO } from '../../../../app/dto/auth/UserDTO';

@Component({
  selector: 'app-user-management',
  templateUrl: './user-management.component.html',
  styleUrls: ['./user-management.component.css']
})
export class UserManagementComponent implements OnInit {

  users: UserDTO[] = [];
  isLoading: boolean = true;
  errorMessage: string | null = null;

  constructor(private userService: UserService) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.isLoading = true;
    this.errorMessage = null;

    this.userService.getAllUsers(0, 10).subscribe({
      next: (data: Page<UserDTO>) => {
        this.users = data.content;
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Lỗi khi tải user!';
        this.isLoading = false;
        console.error(err);
      }
    });
  }

  viewUserDetails(userId: number): void {
    console.log('View user details:', userId);
    // TODO: Navigate to user details page
  }

  editUser(userId: number): void {
    console.log('Edit user:', userId);
    // TODO: Open edit modal or navigate to edit page
    alert('Edit user functionality will be implemented soon');
  }

  deleteUser(userId: number): void {
    if (confirm('Are you sure you want to delete this user?')) {
      console.log('Delete user:', userId);
      // TODO: Call delete API
      alert('Delete user functionality will be implemented soon');
    }
  }
}
