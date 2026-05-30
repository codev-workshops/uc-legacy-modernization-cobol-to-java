import { useState } from 'react';
import { Link } from 'react-router-dom';
import { payBill, Transaction } from '../api/transactions';

export function BillPayment() {
  const [cardNum, setCardNum] = useState('');
  const [amount, setAmount] = useState('');
  const [result, setResult] = useState<Transaction | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setResult(null);
    try {
      const payment = await payBill({
        cardNum,
        amount: Number(amount),
      });
      setResult(payment);
    } catch (err: unknown) {
      const axiosErr = err as { response?: { data?: { message?: string } } };
      setError(axiosErr.response?.data?.message || 'Payment failed');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="bill-payment">
      <h1>Bill Payment</h1>
      <form onSubmit={handleSubmit}>
        <div>
          <label htmlFor="cardNum">Card Number: </label>
          <input id="cardNum" value={cardNum} onChange={(e) => setCardNum(e.target.value)} required />
        </div>
        <div>
          <label htmlFor="amount">Amount: </label>
          <input id="amount" type="number" step="0.01" value={amount} onChange={(e) => setAmount(e.target.value)} required />
        </div>
        <button type="submit" disabled={loading}>{loading ? 'Processing...' : 'Pay'}</button>
      </form>

      {error && <div className="error">{error}</div>}
      {result && (
        <div className="success">
          <p>Payment processed: {result.tranId}</p>
          <Link to={`/transaction-view/${result.tranId}`}>View Transaction</Link>
        </div>
      )}
      <br />
      <Link to="/menu">Back to Menu</Link>
    </div>
  );
}
