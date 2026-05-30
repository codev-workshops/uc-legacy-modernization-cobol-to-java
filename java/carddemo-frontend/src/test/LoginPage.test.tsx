import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { describe, it, expect, beforeAll, afterEach, afterAll } from 'vitest';
import { AuthProvider } from '../context/AuthContext';
import { LoginPage } from '../pages/LoginPage';
import { server } from './mocks/server';

beforeAll(() => server.listen());
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

function renderLogin() {
  return render(
    <AuthProvider>
      <MemoryRouter initialEntries={['/login']}>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/admin" element={<div>Admin Menu</div>} />
          <Route path="/menu" element={<div>Main Menu</div>} />
        </Routes>
      </MemoryRouter>
    </AuthProvider>
  );
}

describe('LoginPage', () => {
  it('renders login form', () => {
    renderLogin();
    expect(screen.getByLabelText(/user id/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /sign in/i })).toBeInTheDocument();
  });

  it('shows error on invalid credentials', async () => {
    renderLogin();
    const user = userEvent.setup();

    await user.type(screen.getByLabelText(/user id/i), 'baduser');
    await user.type(screen.getByLabelText(/password/i), 'badpass');
    await user.click(screen.getByRole('button', { name: /sign in/i }));

    await waitFor(() => {
      expect(screen.getByRole('alert')).toHaveTextContent(/invalid/i);
    });
  });

  it('successful login as admin navigates to admin menu', async () => {
    renderLogin();
    const user = userEvent.setup();

    await user.type(screen.getByLabelText(/user id/i), 'admin01');
    await user.type(screen.getByLabelText(/password/i), 'password');
    await user.click(screen.getByRole('button', { name: /sign in/i }));

    await waitFor(() => {
      expect(screen.getByText('Admin Menu')).toBeInTheDocument();
    });
  });

  it('successful login as user navigates to main menu', async () => {
    renderLogin();
    const user = userEvent.setup();

    await user.type(screen.getByLabelText(/user id/i), 'user0001');
    await user.type(screen.getByLabelText(/password/i), 'password');
    await user.click(screen.getByRole('button', { name: /sign in/i }));

    await waitFor(() => {
      expect(screen.getByText('Main Menu')).toBeInTheDocument();
    });
  });
});
