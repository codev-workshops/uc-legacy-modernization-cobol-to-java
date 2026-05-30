import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, it, expect } from 'vitest';
import { AdminMenu } from '../pages/AdminMenu';
import { AuthProvider } from '../context/AuthContext';

function renderWithAuth(ui: React.ReactElement) {
  return render(
    <AuthProvider>
      <MemoryRouter>
        {ui}
      </MemoryRouter>
    </AuthProvider>
  );
}

describe('AdminMenu', () => {
  it('renders all 6 admin menu items', () => {
    renderWithAuth(<AdminMenu />);

    expect(screen.getByText(/user list \(security\)/i)).toBeInTheDocument();
    expect(screen.getByText(/user add \(security\)/i)).toBeInTheDocument();
    expect(screen.getByText(/user update \(security\)/i)).toBeInTheDocument();
    expect(screen.getByText(/user delete \(security\)/i)).toBeInTheDocument();
    expect(screen.getByText(/transaction type list\/update \(db2\)/i)).toBeInTheDocument();
    expect(screen.getByText(/transaction type maintenance \(db2\)/i)).toBeInTheDocument();
  });

  it('has link to main menu', () => {
    renderWithAuth(<AdminMenu />);
    expect(screen.getByRole('link', { name: /main menu/i })).toBeInTheDocument();
  });

  it('renders sign out button', () => {
    renderWithAuth(<AdminMenu />);
    expect(screen.getByRole('button', { name: /sign out/i })).toBeInTheDocument();
  });
});
