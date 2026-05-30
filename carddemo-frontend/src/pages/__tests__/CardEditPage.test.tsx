import { describe, it, expect } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render } from '@testing-library/react';
import { AuthProvider } from '../../context/AuthContext';
import { CardEditPage } from '../CardEditPage';

function renderPage() {
  localStorage.setItem('token', 'mock-jwt-token');
  localStorage.setItem('user', JSON.stringify({ userId: 'test' }));
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(
    <QueryClientProvider client={qc}>
      <AuthProvider>
        <MemoryRouter initialEntries={['/cards/4111111111111111/edit']}>
          <Routes>
            <Route path="/cards/:num/edit" element={<CardEditPage />} />
            <Route path="/cards/:num" element={<div>Card Detail</div>} />
          </Routes>
        </MemoryRouter>
      </AuthProvider>
    </QueryClientProvider>,
  );
}

describe('CardEditPage', () => {
  it('renders edit form', async () => {
    renderPage();
    await waitFor(() => {
      expect(screen.getByText('Edit Card')).toBeInTheDocument();
    });
    expect(screen.getByLabelText('Card Number')).toBeDisabled();
    expect(screen.getByLabelText('Cardholder Name')).toHaveValue('John Doe');
    expect(screen.getByLabelText('Status')).toHaveValue('ACTIVE');
  });

  it('submits form and navigates to detail', async () => {
    renderPage();
    await waitFor(() => {
      expect(screen.getByText('Save')).toBeInTheDocument();
    });
    await userEvent.click(screen.getByText('Save'));
    await waitFor(() => {
      expect(screen.getByText('Card Detail')).toBeInTheDocument();
    });
  });

  it('cancels and navigates back', async () => {
    renderPage();
    await waitFor(() => {
      expect(screen.getByText('Cancel')).toBeInTheDocument();
    });
    await userEvent.click(screen.getByText('Cancel'));
    await waitFor(() => {
      expect(screen.getByText('Card Detail')).toBeInTheDocument();
    });
  });
});
