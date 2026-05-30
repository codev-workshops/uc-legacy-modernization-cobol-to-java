import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { getTransactions, Transaction, TransactionPage } from '../api/transactions';

export function TransactionList() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [acctIdInput, setAcctIdInput] = useState(searchParams.get('acctId') || '');
  const [page, setPage] = useState<TransactionPage | null>(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const acctId = searchParams.get('acctId');

  useEffect(() => {
    if (!acctId) return;
    setLoading(true);
    setError(null);
    getTransactions(Number(acctId), currentPage, 10)
      .then(setPage)
      .catch((err) => setError(err.response?.data?.message || 'Failed to load transactions'))
      .finally(() => setLoading(false));
  }, [acctId, currentPage]);

  function handleSearch(e: React.FormEvent) {
    e.preventDefault();
    if (acctIdInput) {
      setCurrentPage(0);
      setSearchParams({ acctId: acctIdInput });
    }
  }

  return (
    <div className="transaction-list">
      <h1>Transaction List</h1>
      <form onSubmit={handleSearch}>
        <label htmlFor="acctId">Account ID: </label>
        <input
          id="acctId"
          value={acctIdInput}
          onChange={(e) => setAcctIdInput(e.target.value)}
          placeholder="Enter Account ID"
        />
        <button type="submit">Search</button>
      </form>

      {loading && <div className="loading">Loading...</div>}
      {error && <div className="error">{error}</div>}

      {page && page.content.length > 0 && (
        <>
          <table>
            <thead>
              <tr>
                <th>Transaction ID</th>
                <th>Type</th>
                <th>Amount</th>
                <th>Merchant</th>
                <th>Date</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {page.content.map((txn: Transaction) => (
                <tr key={txn.tranId}>
                  <td>{txn.tranId}</td>
                  <td>{txn.tranTypeCd}</td>
                  <td>${txn.tranAmt?.toFixed(2)}</td>
                  <td>{txn.tranMerchantName}</td>
                  <td>{txn.tranOrigTs}</td>
                  <td><Link to={`/transaction-view/${txn.tranId}`}>View</Link></td>
                </tr>
              ))}
            </tbody>
          </table>
          <div className="pagination">
            <button disabled={currentPage === 0} onClick={() => setCurrentPage(currentPage - 1)}>Previous</button>
            <span> Page {currentPage + 1} of {page.totalPages} </span>
            <button disabled={currentPage >= page.totalPages - 1} onClick={() => setCurrentPage(currentPage + 1)}>Next</button>
          </div>
        </>
      )}

      {page && page.content.length === 0 && <p>No transactions found.</p>}

      <Link to="/menu">Back to Menu</Link>
    </div>
  );
}
