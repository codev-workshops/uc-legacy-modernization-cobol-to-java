import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { useMutation, useQuery } from '@tanstack/react-query';
import { transactionService } from '../services/transactionService';
import { FormField } from '../components/FormField';
import { LoadingSpinner } from '../components/LoadingSpinner';

export function TransactionNewPage() {
  const navigate = useNavigate();
  const [cardNumber, setCardNumber] = useState('');
  const [typeCode, setTypeCode] = useState('');
  const [amount, setAmount] = useState('');
  const [merchantName, setMerchantName] = useState('');
  const [description, setDescription] = useState('');

  const { data: types, isLoading: typesLoading } = useQuery({
    queryKey: ['transactionTypes'],
    queryFn: transactionService.getTransactionTypes,
  });

  const mutation = useMutation({
    mutationFn: transactionService.createTransaction,
    onSuccess: (data) => navigate(`/transactions/${data.id}`),
  });

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    mutation.mutate({
      cardNumber,
      transactionTypeCode: typeCode,
      amount: Number(amount),
      merchantName,
      description,
    });
  };

  if (typesLoading) return <LoadingSpinner />;

  return (
    <div>
      <h1 className="mb-6 text-2xl font-bold text-gray-800">
        New Transaction
      </h1>
      {mutation.error && (
        <div className="mb-4 rounded bg-red-50 p-3 text-sm text-red-700">
          Failed to create transaction
        </div>
      )}
      <form
        onSubmit={handleSubmit}
        className="max-w-lg rounded-lg bg-white p-6 shadow"
      >
        <FormField
          label="Card Number"
          value={cardNumber}
          onChange={(e) => setCardNumber(e.target.value)}
          required
        />
        <div className="mb-4">
          <label
            htmlFor="type-code"
            className="mb-1 block text-sm font-medium text-gray-700"
          >
            Transaction Type
          </label>
          <select
            id="type-code"
            value={typeCode}
            onChange={(e) => setTypeCode(e.target.value)}
            required
            className="w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="">Select type</option>
            {types?.map((t) => (
              <option key={t.typeCode} value={t.typeCode}>
                {t.typeDescription}
              </option>
            ))}
          </select>
        </div>
        <FormField
          label="Amount"
          type="number"
          step="0.01"
          value={amount}
          onChange={(e) => setAmount(e.target.value)}
          required
        />
        <FormField
          label="Merchant Name"
          value={merchantName}
          onChange={(e) => setMerchantName(e.target.value)}
          required
        />
        <FormField
          label="Description"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
        />
        <div className="flex gap-3">
          <button
            type="submit"
            disabled={mutation.isPending}
            className="rounded bg-blue-600 px-4 py-2 text-white hover:bg-blue-700 disabled:opacity-50"
          >
            {mutation.isPending ? 'Creating...' : 'Create'}
          </button>
          <button
            type="button"
            onClick={() => navigate('/transactions')}
            className="rounded bg-gray-200 px-4 py-2 text-gray-700 hover:bg-gray-300"
          >
            Cancel
          </button>
        </div>
      </form>
    </div>
  );
}
