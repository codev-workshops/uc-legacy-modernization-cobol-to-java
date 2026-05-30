import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { http, HttpResponse } from 'msw';
import { setupServer } from 'msw/node';
import { CardList } from '../pages/CardList';
import { AuthProvider } from '../context/AuthContext';
import { setTokenGetter } from '../api/client';
import { beforeAll, afterAll, afterEach, describe, it, expect } from 'vitest';

const mockCards = [
  {
    cardNum: '4111111111111111',
    accountId: 1,
    cvvCode: 123,
    embossedName: 'JOHN DOE',
    expirationDate: '2026-06-30',
    activeStatus: 'Y',
  },
  {
    cardNum: '4111111111112222',
    accountId: 1,
    cvvCode: 456,
    embossedName: 'JANE DOE',
    expirationDate: '2027-01-31',
    activeStatus: 'Y',
  },
];

const server = setupServer(
  http.get('/api/cards', ({ request }) => {
    const url = new URL(request.url);
    const acctId = url.searchParams.get('acctId');
    if (acctId === '1') {
      return HttpResponse.json(mockCards);
    }
    return HttpResponse.json([]);
  })
);

beforeAll(() => {
  server.listen();
  setTokenGetter(() => 'mock-token');
});
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

describe('CardList', () => {
  it('should render card list table', async () => {
    render(
      <AuthProvider>
        <MemoryRouter initialEntries={['/card-list?acctId=1']}>
          <Routes>
            <Route path="/card-list" element={<CardList />} />
          </Routes>
        </MemoryRouter>
      </AuthProvider>
    );

    await waitFor(() => {
      expect(screen.getByText('Cards for Account 1')).toBeInTheDocument();
    });
    expect(screen.getByText('4111111111111111')).toBeInTheDocument();
    expect(screen.getByText('JOHN DOE')).toBeInTheDocument();
    expect(screen.getByText('4111111111112222')).toBeInTheDocument();
  });

  it('should show error when no account ID', async () => {
    render(
      <AuthProvider>
        <MemoryRouter initialEntries={['/card-list']}>
          <Routes>
            <Route path="/card-list" element={<CardList />} />
          </Routes>
        </MemoryRouter>
      </AuthProvider>
    );

    await waitFor(() => {
      expect(screen.getByText('Account ID is required')).toBeInTheDocument();
    });
  });
});
