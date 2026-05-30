import { describe, it, expect } from 'vitest';
import { authService } from '../authService';

describe('authService', () => {
  it('login returns auth response', async () => {
    const result = await authService.login({
      userId: 'admin01',
      password: 'password',
    });
    expect(result.token).toBe('mock-jwt-token');
    expect(result.refreshToken).toBe('mock-refresh-token');
    expect(result.user.userId).toBe('admin01');
  });

  it('refresh returns new token', async () => {
    localStorage.setItem('token', 'old-token');
    const result = await authService.refresh('mock-refresh-token');
    expect(result.token).toBe('mock-jwt-token-refreshed');
  });

  it('getUsers returns user list', async () => {
    localStorage.setItem('token', 'mock-jwt-token');
    const users = await authService.getUsers();
    expect(users).toHaveLength(3);
    expect(users[0].userId).toBe('admin01');
  });

  it('getUser returns specific user', async () => {
    localStorage.setItem('token', 'mock-jwt-token');
    const user = await authService.getUser('admin01');
    expect(user.userId).toBe('admin01');
  });

  it('createUser creates a new user', async () => {
    localStorage.setItem('token', 'mock-jwt-token');
    const user = await authService.createUser({
      firstName: 'New',
      lastName: 'User',
      userType: 'USER',
      password: 'pass123',
    });
    expect(user.firstName).toBe('New');
  });

  it('updateUser updates user', async () => {
    localStorage.setItem('token', 'mock-jwt-token');
    const user = await authService.updateUser('admin01', {
      firstName: 'Updated',
    });
    expect(user.firstName).toBe('Updated');
  });

  it('deleteUser does not throw', async () => {
    localStorage.setItem('token', 'mock-jwt-token');
    await expect(authService.deleteUser('user01')).resolves.toBeUndefined();
  });
});
