import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { accountService } from '../services/accountService';
import { DataTable } from '../components/DataTable';
import { LoadingSpinner } from '../components/LoadingSpinner';
import type { Card } from '../types';

export function CardListPage() {
  const [search, setSearch] = useState('');
  const navigate = useNavigate();

  const { data: cards, isLoading, error } = useQuery({
    queryKey: ['cards'],
    queryFn: accountService.getCards,
  });

  const filtered = (cards ?? []).filter(
    (c) =>
      c.cardNumber.includes(search) ||
      c.cardholderName.toLowerCase().includes(search.toLowerCase()),
  );

  if (isLoading) return <LoadingSpinner />;
  if (error) return <div className="p-4 text-red-600">Failed to load cards</div>;

  return (
    <div>
      <h1 className="mb-4 text-2xl font-bold text-gray-800">Cards</h1>
      <div className="mb-4">
        <input
          type="text"
          placeholder="Search by card number or cardholder..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="w-full max-w-md rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
      </div>
      <div className="rounded-lg bg-white shadow">
        <DataTable<Card>
          columns={[
            { key: 'cardNumber', header: 'Card Number' },
            { key: 'cardholderName', header: 'Cardholder' },
            { key: 'cardType', header: 'Type' },
            { key: 'status', header: 'Status' },
            { key: 'expirationDate', header: 'Expires' },
          ]}
          data={filtered}
          keyExtractor={(c) => c.cardNumber}
          onRowClick={(c) => navigate(`/cards/${c.cardNumber}`)}
        />
      </div>
    </div>
  );
}
