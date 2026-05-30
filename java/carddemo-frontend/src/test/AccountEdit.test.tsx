import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { http, HttpResponse } from 'msw';
import { setupServer } from 'msw/node';
import { AccountEdit } from '../pages/AccountEdit';
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
  http.put('/api/accounts/1', async ({ request }) => {
    const body = await request.json() as Record<string, unknown>;
    return HttpResponse.json({ ...mockAccount, ...body });
  })
);

beforeAll(() => {
  server.listen();
  setTokenGetter(() => 'mock-token');
});
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

describe('AccountEdit', () => {
  it('should render edit form with account data', async () => {
    render(
      <AuthProvider>
        <MemoryRouter initialEntries={['/account-update/1']}>
          <Routes>
            <Route path="/account-update/:id" element={<AccountEdit />} />
          </Routes>
        </MemoryRouter>
      </AuthProvider>
    );

    await waitFor(() => {
      expect(screen.getByText('Edit Account 1')).toBeInTheDocument();
    });
    const statusInput = screen.getByLabelText('Status:') as HTMLInputElement;
    expect(statusInput.value).toBe('Y');
  });

  it('should submit form successfully', async () => {
    const user = userEvent.setup();
    render(
      <AuthProvider>
        <MemoryRouter initialEntries={['/account-update/1']}>
          <Routes>
            <Route path="/account-update/:id" element={<AccountEdit />} />
            <Route path="/account-view/:id" element={<AccountView />} />
          </Routes>
        </MemoryRouter>
      </AuthProvider>
    );

    await waitFor(() => {
      expect(screen.getByText('Edit Account 1')).toBeInTheDocument();
    });

    const statusInput = screen.getByLabelText('Status:');
    await user.clear(statusInput);
    await user.type(statusInput, 'N');
    await user.click(screen.getByText('Save'));

    await waitFor(() => {
      expect(screen.getByText('Account Details')).toBeInTheDocument();
    });
  });
});
