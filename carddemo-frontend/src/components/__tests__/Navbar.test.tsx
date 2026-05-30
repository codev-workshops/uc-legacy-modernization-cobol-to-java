import { describe, it, expect } from 'vitest';
import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { renderWithProviders, loginTestUser } from '../../test/testUtils';
import { Navbar } from '../Navbar';

describe('Navbar', () => {
  it('does not render when not authenticated', () => {
    const { container } = renderWithProviders(<Navbar />);
    expect(container.querySelector('nav')).toBeNull();
  });

  it('renders when authenticated', () => {
    loginTestUser();
    renderWithProviders(<Navbar />);
    expect(screen.getByText('CardDemo')).toBeInTheDocument();
    expect(screen.getByText('John Admin')).toBeInTheDocument();
    expect(screen.getByText('Logout')).toBeInTheDocument();
  });

  it('clears auth on logout click', async () => {
    loginTestUser();
    renderWithProviders(<Navbar />);
    await userEvent.click(screen.getByText('Logout'));
    expect(localStorage.getItem('token')).toBeNull();
  });
});
