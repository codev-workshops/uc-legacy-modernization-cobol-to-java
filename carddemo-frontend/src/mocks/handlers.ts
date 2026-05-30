import { http, HttpResponse } from 'msw';
import {
  mockUser,
  mockUsers,
  mockAccount,
  mockCards,
  mockTransactionPage,
  mockTransactions,
  mockTransactionTypes,
  mockBilling,
  mockReport,
} from './data';

export const handlers = [
  http.post('/api/auth/login', async ({ request }) => {
    const body = (await request.json()) as { userId: string; password: string };
    if (body.userId && body.password) {
      return HttpResponse.json({
        token: 'mock-jwt-token',
        refreshToken: 'mock-refresh-token',
        user: mockUser,
      });
    }
    return new HttpResponse(null, { status: 401 });
  }),

  http.post('/api/auth/refresh', () =>
    HttpResponse.json({
      token: 'mock-jwt-token-refreshed',
      refreshToken: 'mock-refresh-token-new',
      user: mockUser,
    }),
  ),

  http.get('/api/users', () => HttpResponse.json(mockUsers)),

  http.get('/api/users/:userId', ({ params }) => {
    const user = mockUsers.find((u) => u.userId === params.userId);
    return user ? HttpResponse.json(user) : new HttpResponse(null, { status: 404 });
  }),

  http.post('/api/users', async ({ request }) => {
    const body = (await request.json()) as Record<string, string>;
    return HttpResponse.json(
      { userId: 'new-user', ...body },
      { status: 201 },
    );
  }),

  http.put('/api/users/:userId', async ({ request, params }) => {
    const body = (await request.json()) as Record<string, string>;
    return HttpResponse.json({ userId: params.userId, ...body });
  }),

  http.delete('/api/users/:userId', () => new HttpResponse(null, { status: 204 })),

  http.get('/api/accounts/:id', () => HttpResponse.json(mockAccount)),

  http.put('/api/accounts/:id', async ({ request }) => {
    const body = (await request.json()) as Record<string, unknown>;
    return HttpResponse.json({ ...mockAccount, ...body });
  }),

  http.get('/api/cards', () => HttpResponse.json(mockCards)),

  http.get('/api/customers/:id', () =>
    HttpResponse.json({
      id: 1,
      firstName: 'John',
      lastName: 'Doe',
      email: 'john@example.com',
      phone: '555-0100',
      address: '123 Main St',
      ssn: '***-**-1234',
    }),
  ),

  http.get('/api/accounts/:id/billing', () => HttpResponse.json(mockBilling)),

  http.get('/api/transactions', () => HttpResponse.json(mockTransactionPage)),

  http.get('/api/transactions/:id', ({ params }) => {
    const txn = mockTransactions.find((t) => t.id === Number(params.id));
    return txn ? HttpResponse.json(txn) : new HttpResponse(null, { status: 404 });
  }),

  http.post('/api/transactions', async ({ request }) => {
    const body = (await request.json()) as Record<string, unknown>;
    return HttpResponse.json(
      { id: 100, status: 'COMPLETED', transactionDate: '2024-09-15', ...body },
      { status: 201 },
    );
  }),

  http.get('/api/transaction-types', () => HttpResponse.json(mockTransactionTypes)),

  http.get('/api/reports/transactions', () => HttpResponse.json(mockReport)),

  http.get('/api/authorizations/summary', () =>
    HttpResponse.json([
      {
        id: 1,
        cardNumber: '4111111111111111',
        amount: 250.0,
        merchantName: 'Test Merchant',
        status: 'PENDING',
        authorizationDate: '2024-09-10',
      },
    ]),
  ),

  http.post('/api/authorizations/:id/mark-fraud', () =>
    new HttpResponse(null, { status: 200 }),
  ),
];
