import { describe, it, expect } from 'vitest';
import { screen, waitFor, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { renderWithProviders, loginTestUser } from '../../test/testUtils';
import { ReportsPage } from '../ReportsPage';

describe('ReportsPage', () => {
  it('renders report form', () => {
    loginTestUser();
    renderWithProviders(<ReportsPage />);
    expect(screen.getByText('Reports')).toBeInTheDocument();
    expect(screen.getByLabelText('Start Date')).toBeInTheDocument();
    expect(screen.getByLabelText('End Date')).toBeInTheDocument();
    expect(screen.getByText('Generate Report')).toBeInTheDocument();
  });

  it('generates report on submit', async () => {
    loginTestUser();
    renderWithProviders(<ReportsPage />);

    fireEvent.change(screen.getByLabelText('Start Date'), {
      target: { value: '2024-01-01' },
    });
    fireEvent.change(screen.getByLabelText('End Date'), {
      target: { value: '2024-09-30' },
    });
    await userEvent.click(screen.getByText('Generate Report'));

    await waitFor(() => {
      expect(screen.getByText('$2500.00')).toBeInTheDocument();
    });
    expect(screen.getByText('Total Transactions')).toBeInTheDocument();
  });
});
