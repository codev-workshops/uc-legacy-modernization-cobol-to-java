import { describe, it, expect } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render } from '@testing-library/react';
import { AuthProvider } from '../../context/AuthContext';
import { CardDetailPage } from '../CardDetailPage';

function renderPage() {
  localStorage.setItem('token', 'mock-jwt-token');
  localStorage.setItem('user', JSON.stringify({ userId: 'test' }));
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(
    <QueryClientProvider client={qc}>
      <AuthProvider>
        <MemoryRouter initialEntries={['/cards/4111111111111111']}>
          <Routes>
            <Route path="/cards/:num" element={<CardDetailPage />} />
          </Routes>
        </MemoryRouter>
      </AuthProvider>
    </QueryClientProvider>,
  );
}

describe('CardDetailPage', () => {
  it('renders card details', async () => {
    renderPage();
    await waitFor(() => {
      expect(screen.getByText('Card Details')).toBeInTheDocument();
    });
    expect(screen.getByText('4111111111111111')).toBeInTheDocument();
    expect(screen.getByText('John Doe')).toBeInTheDocument();
    expect(screen.getByText('VISA')).toBeInTheDocument();
  });

  it('has edit link', async () => {
    renderPage();
    await waitFor(() => {
      expect(screen.getByText('Edit')).toBeInTheDocument();
    });
  });
});
