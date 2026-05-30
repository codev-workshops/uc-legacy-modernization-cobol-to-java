import { useEffect, useState } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import { getCardsByAccount, CardItem } from '../api/accounts';

export function CardList() {
  const [searchParams] = useSearchParams();
  const acctId = searchParams.get('acctId');
  const [cards, setCards] = useState<CardItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!acctId) {
      setError('Account ID is required');
      setLoading(false);
      return;
    }
    setLoading(true);
    getCardsByAccount(Number(acctId))
      .then(setCards)
      .catch((err) => setError(err.response?.data?.message || 'Failed to load cards'))
      .finally(() => setLoading(false));
  }, [acctId]);

  if (loading) return <div className="loading">Loading...</div>;
  if (error) return <div className="error">{error}</div>;

  return (
    <div className="card-list">
      <h1>Cards for Account {acctId}</h1>
      {cards.length === 0 ? (
        <p>No cards found.</p>
      ) : (
        <table>
          <thead>
            <tr>
              <th>Card Number</th>
              <th>Account ID</th>
              <th>Embossed Name</th>
              <th>Expiry</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {cards.map((card) => (
              <tr key={card.cardNum}>
                <td>{card.cardNum}</td>
                <td>{card.accountId}</td>
                <td>{card.embossedName}</td>
                <td>{card.expirationDate}</td>
                <td>{card.activeStatus}</td>
                <td>
                  <Link to={`/card-view/${card.cardNum}`}>View</Link>
                  {' | '}
                  <Link to={`/card-update/${card.cardNum}`}>Edit</Link>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
      <Link to="/menu">Back to Menu</Link>
    </div>
  );
}
