import { describe, it, expect } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render } from '@testing-library/react';
import { AuthProvider } from '../../context/AuthContext';
import { AccountEditPage } from '../AccountEditPage';

function renderPage() {
  localStorage.setItem('token', 'mock-jwt-token');
  localStorage.setItem('user', JSON.stringify({ userId: 'test' }));
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(
    <QueryClientProvider client={qc}>
      <AuthProvider>
        <MemoryRouter initialEntries={['/accounts/1/edit']}>
          <Routes>
            <Route path="/accounts/:id/edit" element={<AccountEditPage />} />
            <Route path="/accounts/:id" element={<div>Account View</div>} />
          </Routes>
        </MemoryRouter>
      </AuthProvider>
    </QueryClientProvider>,
  );
}

describe('AccountEditPage', () => {
  it('renders edit form with account data', async () => {
    renderPage();
    await waitFor(() => {
      expect(screen.getByText('Edit Account')).toBeInTheDocument();
    });
    expect(screen.getByLabelText('Account Number')).toBeDisabled();
    expect(screen.getByLabelText('Status')).toHaveValue('ACTIVE');
    expect(screen.getByLabelText('Credit Limit')).toHaveValue(10000);
  });

  it('has save and cancel buttons', async () => {
    renderPage();
    await waitFor(() => {
      expect(screen.getByText('Save')).toBeInTheDocument();
    });
    expect(screen.getByText('Cancel')).toBeInTheDocument();
  });

  it('submits updated account', async () => {
    renderPage();
    await waitFor(() => {
      expect(screen.getByLabelText('Status')).toHaveValue('ACTIVE');
    });

    await userEvent.clear(screen.getByLabelText('Status'));
    await userEvent.type(screen.getByLabelText('Status'), 'CLOSED');
    await userEvent.click(screen.getByText('Save'));

    await waitFor(() => {
      expect(screen.getByText('Account View')).toBeInTheDocument();
    });
  });

  it('cancels and navigates back', async () => {
    renderPage();
    await waitFor(() => {
      expect(screen.getByText('Cancel')).toBeInTheDocument();
    });
    await userEvent.click(screen.getByText('Cancel'));
    await waitFor(() => {
      expect(screen.getByText('Account View')).toBeInTheDocument();
    });
  });
});
