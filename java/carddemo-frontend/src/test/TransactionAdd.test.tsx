import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { http, HttpResponse } from 'msw';
import { setupServer } from 'msw/node';
import { TransactionAdd } from '../pages/TransactionAdd';
import { AuthProvider } from '../context/AuthContext';
import { setTokenGetter } from '../api/client';
import { beforeAll, afterAll, afterEach, describe, it, expect } from 'vitest';

const server = setupServer(
  http.post('/api/transactions', async () => {
    return HttpResponse.json({
      tranId: 'NEW123',
      tranTypeCd: '01',
      tranCatCd: 1,
      tranAmt: 100.0,
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
      <MemoryRouter initialEntries={['/transaction-add']}>
        <Routes>
          <Route path="/transaction-add" element={<TransactionAdd />} />
          <Route path="/transaction-view/:id" element={<div>Transaction View</div>} />
        </Routes>
      </MemoryRouter>
    </AuthProvider>
  );
}

describe('TransactionAdd', () => {
  it('should render form fields', () => {
    renderComponent();
    expect(screen.getByText('Add Transaction')).toBeInTheDocument();
    expect(screen.getByLabelText(/Card Number/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Type Code/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Category Code/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Amount/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Submit' })).toBeInTheDocument();
  });

  it('should submit and show success', async () => {
    const user = userEvent.setup();
    renderComponent();

    await user.type(screen.getByLabelText(/Card Number/i), '4111111111111111');
    await user.type(screen.getByLabelText(/Type Code/i), '01');
    await user.type(screen.getByLabelText(/Category Code/i), '1');
    await user.type(screen.getByLabelText(/Amount/i), '100');
    await user.click(screen.getByRole('button', { name: 'Submit' }));

    await waitFor(() => {
      expect(screen.getByText(/Transaction created: NEW123/)).toBeInTheDocument();
    });
  });
});
