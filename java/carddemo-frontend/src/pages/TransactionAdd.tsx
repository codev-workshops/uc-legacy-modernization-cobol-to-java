import { useState } from 'react';
import { Link } from 'react-router-dom';
import { createTransaction, Transaction } from '../api/transactions';

export function TransactionAdd() {
  const [cardNum, setCardNum] = useState('');
  const [tranTypeCd, setTranTypeCd] = useState('');
  const [tranCatCd, setTranCatCd] = useState('');
  const [tranAmt, setTranAmt] = useState('');
  const [merchantName, setMerchantName] = useState('');
  const [merchantCity, setMerchantCity] = useState('');
  const [merchantZip, setMerchantZip] = useState('');
  const [source, setSource] = useState('');
  const [desc, setDesc] = useState('');
  const [result, setResult] = useState<Transaction | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setResult(null);
    try {
      const created = await createTransaction({
        cardNum,
        tranTypeCd,
        tranCatCd: Number(tranCatCd),
        tranAmt: Number(tranAmt),
        tranMerchantName: merchantName,
        tranMerchantCity: merchantCity,
        tranMerchantZip: merchantZip,
        tranSource: source,
        tranDesc: desc,
      });
      setResult(created);
    } catch (err: unknown) {
      const axiosErr = err as { response?: { data?: { message?: string } } };
      setError(axiosErr.response?.data?.message || 'Failed to create transaction');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="transaction-add">
      <h1>Add Transaction</h1>
      <form onSubmit={handleSubmit}>
        <div>
          <label htmlFor="cardNum">Card Number: </label>
          <input id="cardNum" value={cardNum} onChange={(e) => setCardNum(e.target.value)} required />
        </div>
        <div>
          <label htmlFor="tranTypeCd">Type Code: </label>
          <input id="tranTypeCd" value={tranTypeCd} onChange={(e) => setTranTypeCd(e.target.value)} required />
        </div>
        <div>
          <label htmlFor="tranCatCd">Category Code: </label>
          <input id="tranCatCd" value={tranCatCd} onChange={(e) => setTranCatCd(e.target.value)} required />
        </div>
        <div>
          <label htmlFor="tranAmt">Amount: </label>
          <input id="tranAmt" type="number" step="0.01" value={tranAmt} onChange={(e) => setTranAmt(e.target.value)} required />
        </div>
        <div>
          <label htmlFor="merchantName">Merchant Name: </label>
          <input id="merchantName" value={merchantName} onChange={(e) => setMerchantName(e.target.value)} />
        </div>
        <div>
          <label htmlFor="merchantCity">Merchant City: </label>
          <input id="merchantCity" value={merchantCity} onChange={(e) => setMerchantCity(e.target.value)} />
        </div>
        <div>
          <label htmlFor="merchantZip">Merchant Zip: </label>
          <input id="merchantZip" value={merchantZip} onChange={(e) => setMerchantZip(e.target.value)} />
        </div>
        <div>
          <label htmlFor="source">Source: </label>
          <input id="source" value={source} onChange={(e) => setSource(e.target.value)} />
        </div>
        <div>
          <label htmlFor="desc">Description: </label>
          <input id="desc" value={desc} onChange={(e) => setDesc(e.target.value)} />
        </div>
        <button type="submit" disabled={loading}>{loading ? 'Submitting...' : 'Submit'}</button>
      </form>

      {error && <div className="error">{error}</div>}
      {result && (
        <div className="success">
          <p>Transaction created: {result.tranId}</p>
          <Link to={`/transaction-view/${result.tranId}`}>View Transaction</Link>
        </div>
      )}
      <br />
      <Link to="/menu">Back to Menu</Link>
    </div>
  );
}
