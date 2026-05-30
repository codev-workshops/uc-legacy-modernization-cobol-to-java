import { describe, it, expect } from 'vitest';
import { screen } from '@testing-library/react';
import { renderWithProviders, loginTestUser } from '../../test/testUtils';
import { DashboardPage } from '../DashboardPage';

describe('DashboardPage', () => {
  it('renders welcome message', () => {
    loginTestUser();
    renderWithProviders(<DashboardPage />);
    expect(screen.getByText('Welcome, John')).toBeInTheDocument();
  });

  it('renders navigation cards', () => {
    loginTestUser();
    renderWithProviders(<DashboardPage />);
    expect(screen.getByText('Cards')).toBeInTheDocument();
    expect(screen.getByText('Transactions')).toBeInTheDocument();
    expect(screen.getByText('Reports')).toBeInTheDocument();
    expect(screen.getByText('Administration')).toBeInTheDocument();
  });

  it('shows descriptions for each card', () => {
    loginTestUser();
    renderWithProviders(<DashboardPage />);
    expect(screen.getByText('View and manage credit cards')).toBeInTheDocument();
    expect(screen.getByText('View and create transactions')).toBeInTheDocument();
  });
});
