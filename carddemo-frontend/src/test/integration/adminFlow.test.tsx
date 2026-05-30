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
import { UserManagementPage } from '../../pages/UserManagementPage';

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
                <Route path="/admin/users" element={<UserManagementPage />} />
              </Route>
            </Routes>
          </MemoryRouter>
        </AuthProvider>
      </QueryClientProvider>
    </ErrorBoundary>,
  );
}

describe('Admin Flow Integration', () => {
  it('user management → add user → form submit', async () => {
    renderApp('/admin/users');

    await waitFor(() => {
      expect(screen.getByText('User Management')).toBeInTheDocument();
    });

    expect(screen.getByText('admin01')).toBeInTheDocument();

    await userEvent.click(screen.getByRole('button', { name: 'Add User' }));
    expect(screen.getByLabelText('First Name')).toBeInTheDocument();

    await userEvent.type(screen.getByLabelText('First Name'), 'New');
    await userEvent.type(screen.getByLabelText('Last Name'), 'User');
    await userEvent.type(screen.getByLabelText('User Type'), 'USER');
    await userEvent.type(screen.getByLabelText('Password'), 'pass123');
    await userEvent.click(screen.getByRole('button', { name: 'Create' }));

    await waitFor(() => {
      expect(screen.queryByLabelText('First Name')).not.toBeInTheDocument();
    });
  });

  it('user management → edit user → cancel', async () => {
    renderApp('/admin/users');

    await waitFor(() => {
      expect(screen.getAllByText('Edit').length).toBeGreaterThan(0);
    });

    await userEvent.click(screen.getAllByText('Edit')[0]);
    expect(screen.getByText('Edit User')).toBeInTheDocument();
    await userEvent.click(screen.getByText('Cancel'));
    expect(screen.queryByText('Edit User')).not.toBeInTheDocument();
  });
});
