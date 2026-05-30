import { useParams, Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { accountService } from '../services/accountService';
import { LoadingSpinner } from '../components/LoadingSpinner';

export function CardDetailPage() {
  const { num } = useParams<{ num: string }>();

  const { data: cards, isLoading, error } = useQuery({
    queryKey: ['cards'],
    queryFn: accountService.getCards,
  });

  const card = cards?.find((c) => c.cardNumber === num);

  if (isLoading) return <LoadingSpinner />;
  if (error) return <div className="p-4 text-red-600">Failed to load card</div>;
  if (!card) return <div className="p-4 text-gray-500">Card not found</div>;

  return (
    <div>
      <div className="mb-4 flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-800">Card Details</h1>
        <Link
          to={`/cards/${num}/edit`}
          className="rounded bg-blue-600 px-4 py-2 text-sm text-white hover:bg-blue-700"
        >
          Edit
        </Link>
      </div>
      <div className="rounded-lg bg-white p-6 shadow">
        <dl className="grid gap-4 sm:grid-cols-2">
          <div>
            <dt className="text-sm font-medium text-gray-500">Card Number</dt>
            <dd className="text-lg text-gray-900">{card.cardNumber}</dd>
          </div>
          <div>
            <dt className="text-sm font-medium text-gray-500">Cardholder</dt>
            <dd className="text-lg text-gray-900">{card.cardholderName}</dd>
          </div>
          <div>
            <dt className="text-sm font-medium text-gray-500">Type</dt>
            <dd className="text-lg text-gray-900">{card.cardType}</dd>
          </div>
          <div>
            <dt className="text-sm font-medium text-gray-500">Status</dt>
            <dd className="text-lg text-gray-900">{card.status}</dd>
          </div>
          <div>
            <dt className="text-sm font-medium text-gray-500">Account ID</dt>
            <dd className="text-lg text-gray-900">
              <Link to={`/accounts/${card.accountId}`} className="text-blue-600 hover:underline">
                {card.accountId}
              </Link>
            </dd>
          </div>
          <div>
            <dt className="text-sm font-medium text-gray-500">Expiration</dt>
            <dd className="text-lg text-gray-900">{card.expirationDate}</dd>
          </div>
        </dl>
      </div>
    </div>
  );
}
