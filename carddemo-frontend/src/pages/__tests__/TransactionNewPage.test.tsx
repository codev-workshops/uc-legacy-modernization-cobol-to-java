import { describe, it, expect } from 'vitest';
import { screen, waitFor, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render } from '@testing-library/react';
import { AuthProvider } from '../../context/AuthContext';
import { TransactionNewPage } from '../TransactionNewPage';

function renderPage() {
  localStorage.setItem('token', 'mock-jwt-token');
  localStorage.setItem('user', JSON.stringify({ userId: 'test' }));
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(
    <QueryClientProvider client={qc}>
      <AuthProvider>
        <MemoryRouter initialEntries={['/transactions/new']}>
          <Routes>
            <Route path="/transactions/new" element={<TransactionNewPage />} />
            <Route path="/transactions/:id" element={<div>Transaction Detail</div>} />
            <Route path="/transactions" element={<div>Transaction List</div>} />
          </Routes>
        </MemoryRouter>
      </AuthProvider>
    </QueryClientProvider>,
  );
}

describe('TransactionNewPage', () => {
  it('renders new transaction form', async () => {
    renderPage();
    await waitFor(() => {
      expect(screen.getByText('New Transaction')).toBeInTheDocument();
    });
    expect(screen.getByLabelText('Card Number')).toBeInTheDocument();
    expect(screen.getByLabelText('Amount')).toBeInTheDocument();
    expect(screen.getByLabelText('Merchant Name')).toBeInTheDocument();
  });

  it('shows transaction types in dropdown', async () => {
    renderPage();
    await waitFor(() => {
      expect(screen.getByText('Purchase')).toBeInTheDocument();
    });
    expect(screen.getByText('Payment')).toBeInTheDocument();
    expect(screen.getByText('Refund')).toBeInTheDocument();
  });

  it('submits new transaction form', async () => {
    renderPage();
    await waitFor(() => {
      expect(screen.getByLabelText('Card Number')).toBeInTheDocument();
    });

    await userEvent.type(screen.getByLabelText('Card Number'), '4111111111111111');
    fireEvent.change(screen.getByLabelText('Transaction Type'), {
      target: { value: 'PUR' },
    });
    await userEvent.type(screen.getByLabelText('Amount'), '99.99');
    await userEvent.type(screen.getByLabelText('Merchant Name'), 'Test Store');
    await userEvent.type(screen.getByLabelText('Description'), 'Test');

    await userEvent.click(screen.getByRole('button', { name: 'Create' }));

    await waitFor(() => {
      expect(screen.getByText('Transaction Detail')).toBeInTheDocument();
    });
  });

  it('navigates back on cancel', async () => {
    renderPage();
    await waitFor(() => {
      expect(screen.getByText('Cancel')).toBeInTheDocument();
    });
    await userEvent.click(screen.getByText('Cancel'));
    await waitFor(() => {
      expect(screen.getByText('Transaction List')).toBeInTheDocument();
    });
  });
});
