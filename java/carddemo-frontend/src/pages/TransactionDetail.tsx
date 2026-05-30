import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getTransaction, Transaction } from '../api/transactions';

export function TransactionDetail() {
  const { id } = useParams<{ id: string }>();
  const [transaction, setTransaction] = useState<Transaction | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;
    setLoading(true);
    getTransaction(id)
      .then(setTransaction)
      .catch((err) => setError(err.response?.data?.message || 'Failed to load transaction'))
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) return <div className="loading">Loading...</div>;
  if (error) return <div className="error">{error}</div>;
  if (!transaction) return <div className="error">Transaction not found</div>;

  return (
    <div className="transaction-detail">
      <h1>Transaction Details</h1>
      <table>
        <tbody>
          <tr><td>Transaction ID</td><td>{transaction.tranId}</td></tr>
          <tr><td>Type Code</td><td>{transaction.tranTypeCd}</td></tr>
          <tr><td>Category Code</td><td>{transaction.tranCatCd}</td></tr>
          <tr><td>Source</td><td>{transaction.tranSource}</td></tr>
          <tr><td>Description</td><td>{transaction.tranDesc}</td></tr>
          <tr><td>Amount</td><td>${transaction.tranAmt?.toFixed(2)}</td></tr>
          <tr><td>Card Number</td><td>{transaction.tranCardNum}</td></tr>
          <tr><td>Merchant Name</td><td>{transaction.tranMerchantName}</td></tr>
          <tr><td>Merchant City</td><td>{transaction.tranMerchantCity}</td></tr>
          <tr><td>Merchant Zip</td><td>{transaction.tranMerchantZip}</td></tr>
          <tr><td>Origination Time</td><td>{transaction.tranOrigTs}</td></tr>
          <tr><td>Processing Time</td><td>{transaction.tranProcTs}</td></tr>
        </tbody>
      </table>
      <Link to="/transaction-list">Back to Transaction List</Link>
    </div>
  );
}
