import { useParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { accountService } from '../services/accountService';
import { DataTable } from '../components/DataTable';
import { LoadingSpinner } from '../components/LoadingSpinner';
import type { Transaction } from '../types';

export function BillingPage() {
  const { id } = useParams<{ id: string }>();
  const accountId = Number(id);

  const { data: billing, isLoading, error } = useQuery({
    queryKey: ['billing', accountId],
    queryFn: () => accountService.getBilling(accountId),
    enabled: !isNaN(accountId),
  });

  if (isLoading) return <LoadingSpinner />;
  if (error) return <div className="p-4 text-red-600">Failed to load billing</div>;
  if (!billing) return <div className="p-4 text-gray-500">No billing data</div>;

  return (
    <div>
      <h1 className="mb-6 text-2xl font-bold text-gray-800">
        Billing Statement
      </h1>
      <div className="mb-6 rounded-lg bg-white p-6 shadow">
        <dl className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          <div>
            <dt className="text-sm font-medium text-gray-500">Statement Date</dt>
            <dd className="text-lg text-gray-900">{billing.statementDate}</dd>
          </div>
          <div>
            <dt className="text-sm font-medium text-gray-500">Due Date</dt>
            <dd className="text-lg text-gray-900">{billing.dueDate}</dd>
          </div>
          <div>
            <dt className="text-sm font-medium text-gray-500">Total Balance</dt>
            <dd className="text-lg text-gray-900">
              ${billing.totalBalance.toFixed(2)}
            </dd>
          </div>
          <div>
            <dt className="text-sm font-medium text-gray-500">Minimum Payment</dt>
            <dd className="text-lg text-gray-900">
              ${billing.minimumPayment.toFixed(2)}
            </dd>
          </div>
        </dl>
      </div>
      <h2 className="mb-3 text-lg font-semibold text-gray-700">
        Transactions
      </h2>
      <div className="rounded-lg bg-white shadow">
        <DataTable<Transaction>
          columns={[
            { key: 'transactionDate', header: 'Date' },
            { key: 'transactionType', header: 'Type' },
            { key: 'merchantName', header: 'Merchant' },
            { key: 'description', header: 'Description' },
            {
              key: 'amount',
              header: 'Amount',
              render: (t) => `$${t.amount.toFixed(2)}`,
            },
          ]}
          data={billing.transactions}
          keyExtractor={(t) => t.id}
        />
      </div>
    </div>
  );
}
