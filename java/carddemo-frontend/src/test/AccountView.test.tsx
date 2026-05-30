import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { http, HttpResponse } from 'msw';
import { setupServer } from 'msw/node';
import { AccountView } from '../pages/AccountView';
import { AuthProvider } from '../context/AuthContext';
import { setTokenGetter } from '../api/client';
import { beforeAll, afterAll, afterEach, describe, it, expect } from 'vitest';

const mockAccount = {
  accountId: 1,
  activeStatus: 'Y',
  currentBalance: 5000.0,
  creditLimit: 10000.0,
  cashCreditLimit: 3000.0,
  openDate: '2020-01-15',
  expirationDate: '2025-12-31',
  reissueDate: null,
  currentCycleCredit: 100.0,
  currentCycleDebit: 50.0,
  addressZip: '10001',
  groupId: 'GRP01',
};

const server = setupServer(
  http.get('/api/accounts/1', () => HttpResponse.json(mockAccount)),
  http.get('/api/accounts/999', () => HttpResponse.json({ message: 'Not found' }, { status: 404 }))
);

beforeAll(() => {
  server.listen();
  setTokenGetter(() => 'mock-token');
});
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

function renderWithRouter(id: string) {
  return render(
    <AuthProvider>
      <MemoryRouter initialEntries={[`/account-view/${id}`]}>
        <Routes>
          <Route path="/account-view/:id" element={<AccountView />} />
        </Routes>
      </MemoryRouter>
    </AuthProvider>
  );
}

describe('AccountView', () => {
  it('should render account details', async () => {
    renderWithRouter('1');
    expect(screen.getByText('Loading...')).toBeInTheDocument();
    await waitFor(() => {
      expect(screen.getByText('Account Details')).toBeInTheDocument();
    });
    expect(screen.getByText('$5000.00')).toBeInTheDocument();
    expect(screen.getByText('10001')).toBeInTheDocument();
  });

  it('should show error for non-existent account', async () => {
    renderWithRouter('999');
    await waitFor(() => {
      expect(screen.getByText(/Failed to load account|Not found/)).toBeInTheDocument();
    });
  });
});
