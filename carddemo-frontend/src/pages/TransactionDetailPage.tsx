import { useParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { transactionService } from '../services/transactionService';
import { LoadingSpinner } from '../components/LoadingSpinner';

export function TransactionDetailPage() {
  const { id } = useParams<{ id: string }>();
  const txnId = Number(id);

  const { data: txn, isLoading, error } = useQuery({
    queryKey: ['transaction', txnId],
    queryFn: () => transactionService.getTransaction(txnId),
    enabled: !isNaN(txnId),
  });

  if (isLoading) return <LoadingSpinner />;
  if (error) return <div className="p-4 text-red-600">Failed to load transaction</div>;
  if (!txn) return <div className="p-4 text-gray-500">Transaction not found</div>;

  return (
    <div>
      <h1 className="mb-6 text-2xl font-bold text-gray-800">
        Transaction Details
      </h1>
      <div className="rounded-lg bg-white p-6 shadow">
        <dl className="grid gap-4 sm:grid-cols-2">
          <div>
            <dt className="text-sm font-medium text-gray-500">ID</dt>
            <dd className="text-lg text-gray-900">{txn.id}</dd>
          </div>
          <div>
            <dt className="text-sm font-medium text-gray-500">Card Number</dt>
            <dd className="text-lg text-gray-900">{txn.cardNumber}</dd>
          </div>
          <div>
            <dt className="text-sm font-medium text-gray-500">Type</dt>
            <dd className="text-lg text-gray-900">{txn.transactionType}</dd>
          </div>
          <div>
            <dt className="text-sm font-medium text-gray-500">Amount</dt>
            <dd className="text-lg text-gray-900">${txn.amount.toFixed(2)}</dd>
          </div>
          <div>
            <dt className="text-sm font-medium text-gray-500">Merchant</dt>
            <dd className="text-lg text-gray-900">{txn.merchantName}</dd>
          </div>
          <div>
            <dt className="text-sm font-medium text-gray-500">Date</dt>
            <dd className="text-lg text-gray-900">{txn.transactionDate}</dd>
          </div>
          <div>
            <dt className="text-sm font-medium text-gray-500">Status</dt>
            <dd className="text-lg text-gray-900">{txn.status}</dd>
          </div>
          <div className="sm:col-span-2">
            <dt className="text-sm font-medium text-gray-500">Description</dt>
            <dd className="text-lg text-gray-900">{txn.description}</dd>
          </div>
        </dl>
      </div>
    </div>
  );
}
