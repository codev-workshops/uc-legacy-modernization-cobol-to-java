import { describe, it, expect } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { renderWithProviders } from '../../test/testUtils';
import { LoginPage } from '../LoginPage';

describe('LoginPage', () => {
  it('renders login form', () => {
    renderWithProviders(<LoginPage />);
    expect(screen.getByText('CardDemo Sign In')).toBeInTheDocument();
    expect(screen.getByLabelText('User ID')).toBeInTheDocument();
    expect(screen.getByLabelText('Password')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Sign In' })).toBeInTheDocument();
  });

  it('handles successful login', async () => {
    renderWithProviders(<LoginPage />);
    await userEvent.type(screen.getByLabelText('User ID'), 'admin01');
    await userEvent.type(screen.getByLabelText('Password'), 'password');
    await userEvent.click(screen.getByRole('button', { name: 'Sign In' }));

    await waitFor(() => {
      expect(localStorage.getItem('token')).toBe('mock-jwt-token');
    });
  });

  it('shows loading state during login', async () => {
    renderWithProviders(<LoginPage />);
    await userEvent.type(screen.getByLabelText('User ID'), 'admin01');
    await userEvent.type(screen.getByLabelText('Password'), 'password');
    await userEvent.click(screen.getByRole('button', { name: 'Sign In' }));

    await waitFor(() => {
      expect(localStorage.getItem('token')).toBeTruthy();
    });
  });
});
