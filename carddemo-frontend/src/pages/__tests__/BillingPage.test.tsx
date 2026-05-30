import { describe, it, expect } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render } from '@testing-library/react';
import { AuthProvider } from '../../context/AuthContext';
import { BillingPage } from '../BillingPage';

function renderPage() {
  localStorage.setItem('token', 'mock-jwt-token');
  localStorage.setItem('user', JSON.stringify({ userId: 'test' }));
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(
    <QueryClientProvider client={qc}>
      <AuthProvider>
        <MemoryRouter initialEntries={['/accounts/1/billing']}>
          <Routes>
            <Route path="/accounts/:id/billing" element={<BillingPage />} />
          </Routes>
        </MemoryRouter>
      </AuthProvider>
    </QueryClientProvider>,
  );
}

describe('BillingPage', () => {
  it('renders billing statement', async () => {
    renderPage();
    await waitFor(() => {
      expect(screen.getByText('Billing Statement')).toBeInTheDocument();
    });
    expect(screen.getByText('$5432.10')).toBeInTheDocument();
    expect(screen.getByText('$50.00')).toBeInTheDocument();
    expect(screen.getByText('2024-09-25')).toBeInTheDocument();
  });
});
