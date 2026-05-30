import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { transactionService } from '../services/transactionService';
import { DataTable } from '../components/DataTable';
import { LoadingSpinner } from '../components/LoadingSpinner';
import type { Transaction } from '../types';

export function TransactionListPage() {
  const [page, setPage] = useState(0);
  const navigate = useNavigate();

  const { data, isLoading, error } = useQuery({
    queryKey: ['transactions', page],
    queryFn: () => transactionService.getTransactions(page),
  });

  if (isLoading) return <LoadingSpinner />;
  if (error) return <div className="p-4 text-red-600">Failed to load transactions</div>;

  return (
    <div>
      <div className="mb-4 flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-800">Transactions</h1>
        <button
          onClick={() => navigate('/transactions/new')}
          className="rounded bg-blue-600 px-4 py-2 text-sm text-white hover:bg-blue-700"
        >
          New Transaction
        </button>
      </div>
      <div className="rounded-lg bg-white shadow">
        <DataTable<Transaction>
          columns={[
            { key: 'id', header: 'ID' },
            { key: 'cardNumber', header: 'Card Number' },
            { key: 'transactionType', header: 'Type' },
            {
              key: 'amount',
              header: 'Amount',
              render: (t) => `$${t.amount.toFixed(2)}`,
            },
            { key: 'merchantName', header: 'Merchant' },
            { key: 'transactionDate', header: 'Date' },
            { key: 'status', header: 'Status' },
          ]}
          data={data?.content ?? []}
          keyExtractor={(t) => t.id}
          currentPage={page}
          totalPages={data?.totalPages ?? 0}
          onPageChange={setPage}
          onRowClick={(t) => navigate(`/transactions/${t.id}`)}
        />
      </div>
    </div>
  );
}
