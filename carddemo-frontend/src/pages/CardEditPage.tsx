import { useParams, useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { useState, useEffect, type FormEvent } from 'react';
import { accountService } from '../services/accountService';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { FormField } from '../components/FormField';

export function CardEditPage() {
  const { num } = useParams<{ num: string }>();
  const navigate = useNavigate();

  const { data: cards, isLoading } = useQuery({
    queryKey: ['cards'],
    queryFn: accountService.getCards,
  });

  const card = cards?.find((c) => c.cardNumber === num);

  const [status, setStatus] = useState('');
  const [cardholderName, setCardholderName] = useState('');

  useEffect(() => {
    if (card) {
      setStatus(card.status);
      setCardholderName(card.cardholderName);
    }
  }, [card]);

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    navigate(`/cards/${num}`);
  };

  if (isLoading) return <LoadingSpinner />;
  if (!card) return <div className="p-4 text-gray-500">Card not found</div>;

  return (
    <div>
      <h1 className="mb-6 text-2xl font-bold text-gray-800">Edit Card</h1>
      <form
        onSubmit={handleSubmit}
        className="max-w-lg rounded-lg bg-white p-6 shadow"
      >
        <FormField label="Card Number" value={card.cardNumber} disabled />
        <FormField
          label="Cardholder Name"
          value={cardholderName}
          onChange={(e) => setCardholderName(e.target.value)}
          required
        />
        <FormField
          label="Status"
          value={status}
          onChange={(e) => setStatus(e.target.value)}
          required
        />
        <div className="flex gap-3">
          <button
            type="submit"
            className="rounded bg-blue-600 px-4 py-2 text-white hover:bg-blue-700"
          >
            Save
          </button>
          <button
            type="button"
            onClick={() => navigate(`/cards/${num}`)}
            className="rounded bg-gray-200 px-4 py-2 text-gray-700 hover:bg-gray-300"
          >
            Cancel
          </button>
        </div>
      </form>
    </div>
  );
}
