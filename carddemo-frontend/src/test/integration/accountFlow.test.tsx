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
import { DashboardPage } from '../../pages/DashboardPage';
import { AccountViewPage } from '../../pages/AccountViewPage';
import { AccountEditPage } from '../../pages/AccountEditPage';
import { CardListPage } from '../../pages/CardListPage';
import { LoginPage } from '../../pages/LoginPage';

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
              <Route path="/login" element={<LoginPage />} />
              <Route
                element={
                  <ProtectedRoute>
                    <Layout />
                  </ProtectedRoute>
                }
              >
                <Route path="/dashboard" element={<DashboardPage />} />
                <Route path="/accounts/:id" element={<AccountViewPage />} />
                <Route path="/accounts/:id/edit" element={<AccountEditPage />} />
                <Route path="/cards" element={<CardListPage />} />
              </Route>
            </Routes>
          </MemoryRouter>
        </AuthProvider>
      </QueryClientProvider>
    </ErrorBoundary>,
  );
}

describe('Account Flow Integration', () => {
  it('login → dashboard → navigate to cards', async () => {
    renderApp('/dashboard');

    await waitFor(() => {
      expect(screen.getByText('Welcome, John')).toBeInTheDocument();
    });

    const sidebarCardsLink = screen.getByRole('link', { name: 'Cards' });
    await userEvent.click(sidebarCardsLink);

    await waitFor(() => {
      expect(screen.getByText('4111111111111111')).toBeInTheDocument();
    });
  });

  it('views account details', async () => {
    renderApp('/accounts/1');

    await waitFor(() => {
      expect(screen.getByText('Account Details')).toBeInTheDocument();
    });
    expect(screen.getByText('00000000001')).toBeInTheDocument();
  });

  it('navigates to edit account', async () => {
    renderApp('/accounts/1');

    await waitFor(() => {
      expect(screen.getByText('Edit')).toBeInTheDocument();
    });

    await userEvent.click(screen.getByText('Edit'));

    await waitFor(() => {
      expect(screen.getByText('Edit Account')).toBeInTheDocument();
    });
  });
});
