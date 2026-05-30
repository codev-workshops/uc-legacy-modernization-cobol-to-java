import { describe, it, expect } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { renderWithProviders } from '../../test/testUtils';
import { useAuth } from '../../hooks/useAuth';

function TestComponent() {
  const { user, isAuthenticated, login, logout } = useAuth();
  return (
    <div>
      <p data-testid="status">{isAuthenticated ? 'authenticated' : 'anonymous'}</p>
      <p data-testid="user">{user ? user.firstName : 'none'}</p>
      <button onClick={() => login({ userId: 'admin01', password: 'pass' })}>
        Login
      </button>
      <button onClick={logout}>Logout</button>
    </div>
  );
}

describe('AuthContext', () => {
  it('starts unauthenticated', () => {
    renderWithProviders(<TestComponent />);
    expect(screen.getByTestId('status')).toHaveTextContent('anonymous');
    expect(screen.getByTestId('user')).toHaveTextContent('none');
  });

  it('authenticates on login', async () => {
    renderWithProviders(<TestComponent />);
    await userEvent.click(screen.getByText('Login'));
    await waitFor(() => {
      expect(screen.getByTestId('status')).toHaveTextContent('authenticated');
    });
    expect(screen.getByTestId('user')).toHaveTextContent('John');
    expect(localStorage.getItem('token')).toBe('mock-jwt-token');
  });

  it('clears auth on logout', async () => {
    renderWithProviders(<TestComponent />);
    await userEvent.click(screen.getByText('Login'));
    await waitFor(() => {
      expect(screen.getByTestId('status')).toHaveTextContent('authenticated');
    });
    await userEvent.click(screen.getByText('Logout'));
    expect(screen.getByTestId('status')).toHaveTextContent('anonymous');
    expect(localStorage.getItem('token')).toBeNull();
  });

  it('restores auth from localStorage', () => {
    localStorage.setItem('token', 'stored-token');
    localStorage.setItem(
      'user',
      JSON.stringify({ userId: 'u1', firstName: 'Stored', lastName: 'User', userType: 'USER' }),
    );
    renderWithProviders(<TestComponent />);
    expect(screen.getByTestId('status')).toHaveTextContent('authenticated');
    expect(screen.getByTestId('user')).toHaveTextContent('Stored');
  });
});
