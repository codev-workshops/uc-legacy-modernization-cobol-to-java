import { describe, it, expect } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render } from '@testing-library/react';
import { AuthProvider } from '../../context/AuthContext';
import { TransactionDetailPage } from '../TransactionDetailPage';

function renderPage() {
  localStorage.setItem('token', 'mock-jwt-token');
  localStorage.setItem('user', JSON.stringify({ userId: 'test' }));
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(
    <QueryClientProvider client={qc}>
      <AuthProvider>
        <MemoryRouter initialEntries={['/transactions/1']}>
          <Routes>
            <Route path="/transactions/:id" element={<TransactionDetailPage />} />
          </Routes>
        </MemoryRouter>
      </AuthProvider>
    </QueryClientProvider>,
  );
}

describe('TransactionDetailPage', () => {
  it('renders transaction details', async () => {
    renderPage();
    await waitFor(() => {
      expect(screen.getByText('Transaction Details')).toBeInTheDocument();
    });
    expect(screen.getByText('4111111111111111')).toBeInTheDocument();
    expect(screen.getByText('Merchant 1')).toBeInTheDocument();
  });
});
