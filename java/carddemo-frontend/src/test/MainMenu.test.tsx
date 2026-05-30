import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, it, expect } from 'vitest';
import { MainMenu } from '../pages/MainMenu';
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

describe('MainMenu', () => {
  it('renders all 11 menu items', () => {
    renderWithAuth(<MainMenu />);

    expect(screen.getByText(/account view/i)).toBeInTheDocument();
    expect(screen.getByText(/account update/i)).toBeInTheDocument();
    expect(screen.getByText(/credit card list/i)).toBeInTheDocument();
    expect(screen.getByText(/credit card view/i)).toBeInTheDocument();
    expect(screen.getByText(/credit card update/i)).toBeInTheDocument();
    expect(screen.getByText(/transaction list/i)).toBeInTheDocument();
    expect(screen.getByText(/transaction view/i)).toBeInTheDocument();
    expect(screen.getByText(/transaction add/i)).toBeInTheDocument();
    expect(screen.getByText(/transaction reports/i)).toBeInTheDocument();
    expect(screen.getByText(/bill payment/i)).toBeInTheDocument();
    expect(screen.getByText(/pending authorization view/i)).toBeInTheDocument();
  });

  it('renders sign out button', () => {
    renderWithAuth(<MainMenu />);
    expect(screen.getByRole('button', { name: /sign out/i })).toBeInTheDocument();
  });
});
