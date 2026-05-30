import { useEffect, useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { getAccount, updateAccount, Account, AccountUpdate } from '../api/accounts';

export function AccountEdit() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [account, setAccount] = useState<Account | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [status, setStatus] = useState('');
  const [creditLimit, setCreditLimit] = useState('');
  const [cashCreditLimit, setCashCreditLimit] = useState('');
  const [zip, setZip] = useState('');
  const [groupId, setGroupId] = useState('');

  useEffect(() => {
    if (!id) return;
    setLoading(true);
    getAccount(Number(id))
      .then((acct) => {
        setAccount(acct);
        setStatus(acct.activeStatus || '');
        setCreditLimit(String(acct.creditLimit ?? ''));
        setCashCreditLimit(String(acct.cashCreditLimit ?? ''));
        setZip(acct.addressZip || '');
        setGroupId(acct.groupId || '');
      })
      .catch((err) => setError(err.response?.data?.message || 'Failed to load account'))
      .finally(() => setLoading(false));
  }, [id]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!id) return;
    setSaving(true);
    setError(null);
    try {
      const update: AccountUpdate = {
        activeStatus: status,
        creditLimit: Number(creditLimit),
        cashCreditLimit: Number(cashCreditLimit),
        addressZip: zip,
        groupId,
      };
      await updateAccount(Number(id), update);
      navigate(`/account-view/${id}`);
    } catch (err: unknown) {
      const axiosErr = err as { response?: { data?: { message?: string } } };
      setError(axiosErr.response?.data?.message || 'Failed to update account');
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <div className="loading">Loading...</div>;
  if (error && !account) return <div className="error">{error}</div>;
  if (!account) return <div className="error">Account not found</div>;

  return (
    <div className="account-edit">
      <h1>Edit Account {account.accountId}</h1>
      {error && <div className="error">{error}</div>}
      <form onSubmit={handleSubmit}>
        <label>
          Status:
          <input value={status} onChange={(e) => setStatus(e.target.value)} maxLength={1} />
        </label>
        <label>
          Credit Limit:
          <input type="number" value={creditLimit} onChange={(e) => setCreditLimit(e.target.value)} step="0.01" />
        </label>
        <label>
          Cash Credit Limit:
          <input type="number" value={cashCreditLimit} onChange={(e) => setCashCreditLimit(e.target.value)} step="0.01" />
        </label>
        <label>
          Address Zip:
          <input value={zip} onChange={(e) => setZip(e.target.value)} maxLength={10} />
        </label>
        <label>
          Group ID:
          <input value={groupId} onChange={(e) => setGroupId(e.target.value)} maxLength={10} />
        </label>
        <button type="submit" disabled={saving}>{saving ? 'Saving...' : 'Save'}</button>
      </form>
      <Link to="/menu">Back to Menu</Link>
    </div>
  );
}
