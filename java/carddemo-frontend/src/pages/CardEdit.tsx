import { useEffect, useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { getCard, updateCard, CardItem, CardUpdate } from '../api/accounts';

export function CardEdit() {
  const { num } = useParams<{ num: string }>();
  const navigate = useNavigate();
  const [card, setCard] = useState<CardItem | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [embossedName, setEmbossedName] = useState('');
  const [expirationDate, setExpirationDate] = useState('');
  const [activeStatus, setActiveStatus] = useState('');

  useEffect(() => {
    if (!num) return;
    setLoading(true);
    getCard(num)
      .then((c) => {
        setCard(c);
        setEmbossedName(c.embossedName || '');
        setExpirationDate(c.expirationDate || '');
        setActiveStatus(c.activeStatus || '');
      })
      .catch((err) => setError(err.response?.data?.message || 'Failed to load card'))
      .finally(() => setLoading(false));
  }, [num]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!num) return;
    setSaving(true);
    setError(null);
    try {
      const update: CardUpdate = {
        embossedName,
        expirationDate,
        activeStatus,
      };
      await updateCard(num, update);
      navigate(`/card-view/${num}`);
    } catch (err: unknown) {
      const axiosErr = err as { response?: { data?: { message?: string } } };
      setError(axiosErr.response?.data?.message || 'Failed to update card');
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <div className="loading">Loading...</div>;
  if (error && !card) return <div className="error">{error}</div>;
  if (!card) return <div className="error">Card not found</div>;

  return (
    <div className="card-edit">
      <h1>Edit Card {card.cardNum}</h1>
      {error && <div className="error">{error}</div>}
      <form onSubmit={handleSubmit}>
        <label>
          Embossed Name:
          <input value={embossedName} onChange={(e) => setEmbossedName(e.target.value)} maxLength={50} />
        </label>
        <label>
          Expiration Date:
          <input type="date" value={expirationDate} onChange={(e) => setExpirationDate(e.target.value)} />
        </label>
        <label>
          Status:
          <input value={activeStatus} onChange={(e) => setActiveStatus(e.target.value)} maxLength={1} />
        </label>
        <button type="submit" disabled={saving}>{saving ? 'Saving...' : 'Save'}</button>
      </form>
      <Link to={`/card-view/${card.cardNum}`}>Cancel</Link>
      {' | '}
      <Link to="/menu">Back to Menu</Link>
    </div>
  );
}
