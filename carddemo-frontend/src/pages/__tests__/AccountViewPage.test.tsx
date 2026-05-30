import { describe, it, expect } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render } from '@testing-library/react';
import { AuthProvider } from '../../context/AuthContext';
import { AccountViewPage } from '../AccountViewPage';

function renderPage() {
  localStorage.setItem('token', 'mock-jwt-token');
  localStorage.setItem('user', JSON.stringify({ userId: 'test' }));
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(
    <QueryClientProvider client={qc}>
      <AuthProvider>
        <MemoryRouter initialEntries={['/accounts/1']}>
          <Routes>
            <Route path="/accounts/:id" element={<AccountViewPage />} />
          </Routes>
        </MemoryRouter>
      </AuthProvider>
    </QueryClientProvider>,
  );
}

describe('AccountViewPage', () => {
  it('renders account details', async () => {
    renderPage();
    await waitFor(() => {
      expect(screen.getByText('Account Details')).toBeInTheDocument();
    });
    expect(screen.getByText('00000000001')).toBeInTheDocument();
    expect(screen.getByText('John Doe')).toBeInTheDocument();
    expect(screen.getByText('ACTIVE')).toBeInTheDocument();
    expect(screen.getByText('$5432.10')).toBeInTheDocument();
  });

  it('has edit and billing links', async () => {
    renderPage();
    await waitFor(() => {
      expect(screen.getByText('Edit')).toBeInTheDocument();
    });
    expect(screen.getByText('View Billing Statement')).toBeInTheDocument();
  });
});
