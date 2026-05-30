import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useState, useEffect, type FormEvent } from 'react';
import { accountService } from '../services/accountService';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { FormField } from '../components/FormField';

export function AccountEditPage() {
  const { id } = useParams<{ id: string }>();
  const accountId = Number(id);
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const { data: account, isLoading } = useQuery({
    queryKey: ['account', accountId],
    queryFn: () => accountService.getAccount(accountId),
    enabled: !isNaN(accountId),
  });

  const [status, setStatus] = useState('');
  const [creditLimit, setCreditLimit] = useState('');

  useEffect(() => {
    if (account) {
      setStatus(account.status);
      setCreditLimit(String(account.creditLimit));
    }
  }, [account]);

  const mutation = useMutation({
    mutationFn: (data: { status: string; creditLimit: number }) =>
      accountService.updateAccount(accountId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['account', accountId] });
      navigate(`/accounts/${id}`);
    },
  });

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    mutation.mutate({ status, creditLimit: Number(creditLimit) });
  };

  if (isLoading) return <LoadingSpinner />;

  return (
    <div>
      <h1 className="mb-6 text-2xl font-bold text-gray-800">Edit Account</h1>
      {mutation.error && (
        <div className="mb-4 rounded bg-red-50 p-3 text-sm text-red-700">
          Failed to update account
        </div>
      )}
      <form
        onSubmit={handleSubmit}
        className="max-w-lg rounded-lg bg-white p-6 shadow"
      >
        <FormField
          label="Account Number"
          value={account?.accountNumber ?? ''}
          disabled
        />
        <FormField
          label="Status"
          value={status}
          onChange={(e) => setStatus(e.target.value)}
          required
        />
        <FormField
          label="Credit Limit"
          type="number"
          value={creditLimit}
          onChange={(e) => setCreditLimit(e.target.value)}
          required
        />
        <div className="flex gap-3">
          <button
            type="submit"
            disabled={mutation.isPending}
            className="rounded bg-blue-600 px-4 py-2 text-white hover:bg-blue-700 disabled:opacity-50"
          >
            {mutation.isPending ? 'Saving...' : 'Save'}
          </button>
          <button
            type="button"
            onClick={() => navigate(`/accounts/${id}`)}
            className="rounded bg-gray-200 px-4 py-2 text-gray-700 hover:bg-gray-300"
          >
            Cancel
          </button>
        </div>
      </form>
    </div>
  );
}
