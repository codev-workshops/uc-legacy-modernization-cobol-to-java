import { describe, it, expect } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { renderWithProviders, loginTestUser } from '../../test/testUtils';
import { CardListPage } from '../CardListPage';

describe('CardListPage', () => {
  it('renders card list', async () => {
    loginTestUser();
    renderWithProviders(<CardListPage />);
    await waitFor(() => {
      expect(screen.getByText('Cards')).toBeInTheDocument();
    });
    expect(screen.getByText('4111111111111111')).toBeInTheDocument();
    expect(screen.getByText('5500000000000004')).toBeInTheDocument();
  });

  it('filters cards by search', async () => {
    loginTestUser();
    renderWithProviders(<CardListPage />);
    await waitFor(() => {
      expect(screen.getByText('4111111111111111')).toBeInTheDocument();
    });
    await userEvent.type(
      screen.getByPlaceholderText('Search by card number or cardholder...'),
      '5500',
    );
    expect(screen.queryByText('4111111111111111')).not.toBeInTheDocument();
    expect(screen.getByText('5500000000000004')).toBeInTheDocument();
  });
});
