import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { http, HttpResponse } from 'msw';
import { setupServer } from 'msw/node';
import { TransactionDetail } from '../pages/TransactionDetail';
import { AuthProvider } from '../context/AuthContext';
import { setTokenGetter } from '../api/client';
import { beforeAll, afterAll, afterEach, describe, it, expect } from 'vitest';

const mockTransaction = {
  tranId: 'ABC123',
  tranTypeCd: '01',
  tranCatCd: 1,
  tranSource: 'POS TERM',
  tranDesc: 'Test purchase',
  tranAmt: 250.0,
  tranMerchantId: null,
  tranMerchantName: 'Test Merchant',
  tranMerchantCity: 'TestCity',
  tranMerchantZip: '12345',
  tranCardNum: '4111111111111111',
  tranOrigTs: '2024-06-15T10:00:00',
  tranProcTs: '2024-06-15T10:00:01',
};

const server = setupServer(
  http.get('/api/transactions/ABC123', () => HttpResponse.json(mockTransaction)),
  http.get('/api/transactions/MISSING', () => HttpResponse.json({ message: 'Not found' }, { status: 404 }))
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
      <MemoryRouter initialEntries={[`/transaction-view/${id}`]}>
        <Routes>
          <Route path="/transaction-view/:id" element={<TransactionDetail />} />
        </Routes>
      </MemoryRouter>
    </AuthProvider>
  );
}

describe('TransactionDetail', () => {
  it('should render transaction details', async () => {
    renderWithRouter('ABC123');
    expect(screen.getByText('Loading...')).toBeInTheDocument();
    await waitFor(() => {
      expect(screen.getByText('Transaction Details')).toBeInTheDocument();
    });
    expect(screen.getByText('ABC123')).toBeInTheDocument();
    expect(screen.getByText('$250.00')).toBeInTheDocument();
    expect(screen.getByText('Test Merchant')).toBeInTheDocument();
  });

  it('should show error for non-existent transaction', async () => {
    renderWithRouter('MISSING');
    await waitFor(() => {
      expect(screen.getByText(/Failed to load transaction|Not found/)).toBeInTheDocument();
    });
  });
});
