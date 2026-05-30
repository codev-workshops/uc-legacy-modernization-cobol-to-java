import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider } from '../../context/AuthContext';
import { ProtectedRoute } from '../ProtectedRoute';

function renderWithRouter(initialRoute: string, isLoggedIn: boolean) {
  if (isLoggedIn) {
    localStorage.setItem('token', 'test-token');
    localStorage.setItem('user', JSON.stringify({ userId: 'test' }));
  }
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });
  return render(
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <MemoryRouter initialEntries={[initialRoute]}>
          <Routes>
            <Route path="/login" element={<div>Login Page</div>} />
            <Route
              path="/dashboard"
              element={
                <ProtectedRoute>
                  <div>Protected Content</div>
                </ProtectedRoute>
              }
            />
          </Routes>
        </MemoryRouter>
      </AuthProvider>
    </QueryClientProvider>,
  );
}

describe('ProtectedRoute', () => {
  it('redirects to login when not authenticated', () => {
    renderWithRouter('/dashboard', false);
    expect(screen.getByText('Login Page')).toBeInTheDocument();
  });

  it('renders children when authenticated', () => {
    renderWithRouter('/dashboard', true);
    expect(screen.getByText('Protected Content')).toBeInTheDocument();
  });
});
