import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getAccount, Account } from '../api/accounts';

export function AccountView() {
  const { id } = useParams<{ id: string }>();
  const [account, setAccount] = useState<Account | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;
    setLoading(true);
    getAccount(Number(id))
      .then(setAccount)
      .catch((err) => setError(err.response?.data?.message || 'Failed to load account'))
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) return <div className="loading">Loading...</div>;
  if (error) return <div className="error">{error}</div>;
  if (!account) return <div className="error">Account not found</div>;

  return (
    <div className="account-view">
      <h1>Account Details</h1>
      <table>
        <tbody>
          <tr><td>Account ID</td><td>{account.accountId}</td></tr>
          <tr><td>Status</td><td>{account.activeStatus}</td></tr>
          <tr><td>Current Balance</td><td>${account.currentBalance?.toFixed(2)}</td></tr>
          <tr><td>Credit Limit</td><td>${account.creditLimit?.toFixed(2)}</td></tr>
          <tr><td>Cash Credit Limit</td><td>${account.cashCreditLimit?.toFixed(2)}</td></tr>
          <tr><td>Open Date</td><td>{account.openDate}</td></tr>
          <tr><td>Expiration Date</td><td>{account.expirationDate}</td></tr>
          <tr><td>Address Zip</td><td>{account.addressZip}</td></tr>
          <tr><td>Group ID</td><td>{account.groupId}</td></tr>
        </tbody>
      </table>
      <Link to="/menu">Back to Menu</Link>
    </div>
  );
}
