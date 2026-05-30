import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { http, HttpResponse } from 'msw';
import { setupServer } from 'msw/node';
import { TransactionList } from '../pages/TransactionList';
import { AuthProvider } from '../context/AuthContext';
import { setTokenGetter } from '../api/client';
import { beforeAll, afterAll, afterEach, describe, it, expect } from 'vitest';

const mockPage = {
  content: [
    {
      tranId: '0000000000000001',
      tranTypeCd: '01',
      tranCatCd: 1,
      tranSource: 'POS TERM',
      tranDesc: 'Test purchase',
      tranAmt: 100.0,
      tranMerchantId: null,
      tranMerchantName: 'Test Merchant',
      tranMerchantCity: 'TestCity',
      tranMerchantZip: '12345',
      tranCardNum: '4111111111111111',
      tranOrigTs: '2024-06-15T10:00:00',
      tranProcTs: '2024-06-15T10:00:01',
    },
  ],
  totalPages: 1,
  totalElements: 1,
  number: 0,
  size: 10,
};

const server = setupServer(
  http.get('/api/transactions', ({ request }) => {
    const url = new URL(request.url);
    const acctId = url.searchParams.get('acctId');
    if (acctId === '1') {
      return HttpResponse.json(mockPage);
    }
    return HttpResponse.json({ content: [], totalPages: 0, totalElements: 0, number: 0, size: 10 });
  })
);

beforeAll(() => {
  server.listen();
  setTokenGetter(() => 'mock-token');
});
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

function renderWithRouter(initialEntry = '/transaction-list?acctId=1') {
  return render(
    <AuthProvider>
      <MemoryRouter initialEntries={[initialEntry]}>
        <Routes>
          <Route path="/transaction-list" element={<TransactionList />} />
        </Routes>
      </MemoryRouter>
    </AuthProvider>
  );
}

describe('TransactionList', () => {
  it('should render transaction table', async () => {
    renderWithRouter();
    await waitFor(() => {
      expect(screen.getByText('0000000000000001')).toBeInTheDocument();
    });
    expect(screen.getByText('Test Merchant')).toBeInTheDocument();
    expect(screen.getByText('$100.00')).toBeInTheDocument();
  });

  it('should show no transactions when empty', async () => {
    renderWithRouter('/transaction-list?acctId=999');
    await waitFor(() => {
      expect(screen.getByText('No transactions found.')).toBeInTheDocument();
    });
  });

  it('should render search form', () => {
    renderWithRouter('/transaction-list');
    expect(screen.getByLabelText(/Account ID/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Search' })).toBeInTheDocument();
  });
});
