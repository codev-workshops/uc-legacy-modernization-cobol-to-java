import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { http, HttpResponse } from 'msw';
import { setupServer } from 'msw/node';
import { BillPayment } from '../pages/BillPayment';
import { AuthProvider } from '../context/AuthContext';
import { setTokenGetter } from '../api/client';
import { beforeAll, afterAll, afterEach, describe, it, expect } from 'vitest';

const server = setupServer(
  http.post('/api/billing/pay', async () => {
    return HttpResponse.json({
      tranId: 'PAY123',
      tranTypeCd: '02',
      tranCatCd: 2,
      tranAmt: -100.0,
      tranCardNum: '4111111111111111',
    }, { status: 201 });
  })
);

beforeAll(() => {
  server.listen();
  setTokenGetter(() => 'mock-token');
});
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

function renderComponent() {
  return render(
    <AuthProvider>
      <MemoryRouter initialEntries={['/bill-payment']}>
        <Routes>
          <Route path="/bill-payment" element={<BillPayment />} />
          <Route path="/transaction-view/:id" element={<div>Transaction View</div>} />
        </Routes>
      </MemoryRouter>
    </AuthProvider>
  );
}

describe('BillPayment', () => {
  it('should render payment form', () => {
    renderComponent();
    expect(screen.getByText('Bill Payment')).toBeInTheDocument();
    expect(screen.getByLabelText(/Card Number/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Amount/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Pay' })).toBeInTheDocument();
  });

  it('should submit payment and show success', async () => {
    const user = userEvent.setup();
    renderComponent();

    await user.type(screen.getByLabelText(/Card Number/i), '4111111111111111');
    await user.type(screen.getByLabelText(/Amount/i), '100');
    await user.click(screen.getByRole('button', { name: 'Pay' }));

    await waitFor(() => {
      expect(screen.getByText(/Payment processed: PAY123/)).toBeInTheDocument();
    });
  });
});
