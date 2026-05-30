import { describe, it, expect } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import { renderWithProviders, loginTestUser } from '../../test/testUtils';
import { TransactionListPage } from '../TransactionListPage';

describe('TransactionListPage', () => {
  it('renders transaction list', async () => {
    loginTestUser();
    renderWithProviders(<TransactionListPage />);
    await waitFor(() => {
      expect(screen.getByText('Transactions')).toBeInTheDocument();
    });
    expect(screen.getByText('New Transaction')).toBeInTheDocument();
  });

  it('shows pagination', async () => {
    loginTestUser();
    renderWithProviders(<TransactionListPage />);
    await waitFor(() => {
      expect(screen.getByText('Next')).toBeInTheDocument();
    });
  });
});
