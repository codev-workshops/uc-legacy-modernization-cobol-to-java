import { useParams, Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { accountService } from '../services/accountService';
import { LoadingSpinner } from '../components/LoadingSpinner';

export function AccountViewPage() {
  const { id } = useParams<{ id: string }>();
  const accountId = Number(id);

  const { data: account, isLoading, error } = useQuery({
    queryKey: ['account', accountId],
    queryFn: () => accountService.getAccount(accountId),
    enabled: !isNaN(accountId),
  });

  if (isLoading) return <LoadingSpinner />;
  if (error) return <div className="p-4 text-red-600">Failed to load account</div>;
  if (!account) return <div className="p-4 text-gray-500">Account not found</div>;

  return (
    <div>
      <div className="mb-4 flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-800">Account Details</h1>
        <Link
          to={`/accounts/${id}/edit`}
          className="rounded bg-blue-600 px-4 py-2 text-sm text-white hover:bg-blue-700"
        >
          Edit
        </Link>
      </div>
      <div className="rounded-lg bg-white p-6 shadow">
        <dl className="grid gap-4 sm:grid-cols-2">
          <div>
            <dt className="text-sm font-medium text-gray-500">Account Number</dt>
            <dd className="text-lg text-gray-900">{account.accountNumber}</dd>
          </div>
          <div>
            <dt className="text-sm font-medium text-gray-500">Customer Name</dt>
            <dd className="text-lg text-gray-900">{account.customerName}</dd>
          </div>
          <div>
            <dt className="text-sm font-medium text-gray-500">Status</dt>
            <dd className="text-lg text-gray-900">{account.status}</dd>
          </div>
          <div>
            <dt className="text-sm font-medium text-gray-500">Current Balance</dt>
            <dd className="text-lg text-gray-900">${account.currentBalance.toFixed(2)}</dd>
          </div>
          <div>
            <dt className="text-sm font-medium text-gray-500">Credit Limit</dt>
            <dd className="text-lg text-gray-900">${account.creditLimit.toFixed(2)}</dd>
          </div>
          <div>
            <dt className="text-sm font-medium text-gray-500">Open Date</dt>
            <dd className="text-lg text-gray-900">{account.openDate}</dd>
          </div>
          <div>
            <dt className="text-sm font-medium text-gray-500">Expiration Date</dt>
            <dd className="text-lg text-gray-900">{account.expirationDate}</dd>
          </div>
        </dl>
        <div className="mt-6">
          <Link
            to={`/accounts/${id}/billing`}
            className="text-blue-600 hover:underline"
          >
            View Billing Statement
          </Link>
        </div>
      </div>
    </div>
  );
}
