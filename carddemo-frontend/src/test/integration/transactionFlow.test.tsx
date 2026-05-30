import { describe, it, expect } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { render } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider } from '../../context/AuthContext';
import { ErrorBoundary } from '../../components/ErrorBoundary';
import { ProtectedRoute } from '../../components/ProtectedRoute';
import { Layout } from '../../components/Layout';
import { TransactionListPage } from '../../pages/TransactionListPage';
import { TransactionDetailPage } from '../../pages/TransactionDetailPage';
import { TransactionNewPage } from '../../pages/TransactionNewPage';

function renderApp(route: string) {
  localStorage.setItem('token', 'mock-jwt-token');
  localStorage.setItem('refreshToken', 'mock-refresh-token');
  localStorage.setItem(
    'user',
    JSON.stringify({
      userId: 'admin01',
      firstName: 'John',
      lastName: 'Admin',
      userType: 'ADMIN',
    }),
  );
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(
    <ErrorBoundary>
      <QueryClientProvider client={qc}>
        <AuthProvider>
          <MemoryRouter initialEntries={[route]}>
            <Routes>
              <Route
                element={
                  <ProtectedRoute>
                    <Layout />
                  </ProtectedRoute>
                }
              >
                <Route path="/transactions" element={<TransactionListPage />} />
                <Route path="/transactions/new" element={<TransactionNewPage />} />
                <Route path="/transactions/:id" element={<TransactionDetailPage />} />
              </Route>
            </Routes>
          </MemoryRouter>
        </AuthProvider>
      </QueryClientProvider>
    </ErrorBoundary>,
  );
}

describe('Transaction Flow Integration', () => {
  it('views transaction list and navigates to new transaction', async () => {
    renderApp('/transactions');

    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'New Transaction' })).toBeInTheDocument();
    });

    await userEvent.click(screen.getByRole('button', { name: 'New Transaction' }));

    await waitFor(() => {
      expect(screen.getByLabelText('Card Number')).toBeInTheDocument();
    });
  });

  it('views transaction detail', async () => {
    renderApp('/transactions/1');

    await waitFor(() => {
      expect(screen.getByText('Transaction Details')).toBeInTheDocument();
    });
    expect(screen.getByText('4111111111111111')).toBeInTheDocument();
  });
});
