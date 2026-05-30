import { describe, it, expect } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import { http, HttpResponse } from 'msw';
import { render } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider } from '../../context/AuthContext';
import { ErrorBoundary } from '../../components/ErrorBoundary';
import { ProtectedRoute } from '../../components/ProtectedRoute';
import { Layout } from '../../components/Layout';
import { CardListPage } from '../../pages/CardListPage';
import { LoginPage } from '../../pages/LoginPage';
import { server } from '../../mocks/server';

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
  const qc = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });
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
                <Route path="/cards" element={<CardListPage />} />
              </Route>
            </Routes>
          </MemoryRouter>
        </AuthProvider>
      </QueryClientProvider>
    </ErrorBoundary>,
  );
}

describe('Error Handling Integration', () => {
  it('shows error message on 500', async () => {
    server.use(
      http.get('/api/cards', () => new HttpResponse(null, { status: 500 })),
    );
    renderApp('/cards');

    await waitFor(() => {
      expect(screen.getByText('Failed to load cards')).toBeInTheDocument();
    });
  });

  it('clears token on 401 when refresh fails', async () => {
    server.use(
      http.get('/api/cards', () => new HttpResponse(null, { status: 401 })),
      http.post('/api/auth/refresh', () => new HttpResponse(null, { status: 401 })),
    );

    renderApp('/cards');

    await waitFor(
      () => {
        expect(localStorage.getItem('token')).toBeNull();
      },
      { timeout: 3000 },
    );
  });
});
