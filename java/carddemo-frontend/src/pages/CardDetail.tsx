import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getCard, CardItem } from '../api/accounts';

export function CardDetail() {
  const { num } = useParams<{ num: string }>();
  const [card, setCard] = useState<CardItem | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!num) return;
    setLoading(true);
    getCard(num)
      .then(setCard)
      .catch((err) => setError(err.response?.data?.message || 'Failed to load card'))
      .finally(() => setLoading(false));
  }, [num]);

  if (loading) return <div className="loading">Loading...</div>;
  if (error) return <div className="error">{error}</div>;
  if (!card) return <div className="error">Card not found</div>;

  return (
    <div className="card-detail">
      <h1>Card Details</h1>
      <table>
        <tbody>
          <tr><td>Card Number</td><td>{card.cardNum}</td></tr>
          <tr><td>Account ID</td><td>{card.accountId}</td></tr>
          <tr><td>CVV Code</td><td>{card.cvvCode}</td></tr>
          <tr><td>Embossed Name</td><td>{card.embossedName}</td></tr>
          <tr><td>Expiration Date</td><td>{card.expirationDate}</td></tr>
          <tr><td>Status</td><td>{card.activeStatus}</td></tr>
        </tbody>
      </table>
      <Link to={`/card-update/${card.cardNum}`}>Edit Card</Link>
      {' | '}
      <Link to="/menu">Back to Menu</Link>
    </div>
  );
}
