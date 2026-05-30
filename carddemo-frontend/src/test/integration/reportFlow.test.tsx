import { describe, it, expect } from 'vitest';
import { screen, waitFor, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { render } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider } from '../../context/AuthContext';
import { ErrorBoundary } from '../../components/ErrorBoundary';
import { ProtectedRoute } from '../../components/ProtectedRoute';
import { Layout } from '../../components/Layout';
import { ReportsPage } from '../../pages/ReportsPage';

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
                <Route path="/reports" element={<ReportsPage />} />
              </Route>
            </Routes>
          </MemoryRouter>
        </AuthProvider>
      </QueryClientProvider>
    </ErrorBoundary>,
  );
}

describe('Report Flow Integration', () => {
  it('generates report with date range', async () => {
    renderApp('/reports');

    await waitFor(() => {
      expect(screen.getByRole('heading', { name: 'Reports' })).toBeInTheDocument();
    });

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
  });
});
