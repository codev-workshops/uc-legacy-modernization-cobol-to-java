import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { generateReports, listReports, GeneratedReport } from '../api/reports';

export function Reports() {
  const [reports, setReports] = useState<GeneratedReport[]>([]);
  const [loading, setLoading] = useState(false);
  const [generating, setGenerating] = useState(false);
  const [error, setError] = useState('');
  const [selectedReport, setSelectedReport] = useState<GeneratedReport | null>(null);

  const fetchReports = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await listReports();
      setReports(data);
    } catch {
      setError('Failed to load reports.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchReports();
  }, []);

  const handleGenerate = async () => {
    setGenerating(true);
    setError('');
    try {
      await generateReports();
      await fetchReports();
    } catch {
      setError('Failed to generate reports.');
    } finally {
      setGenerating(false);
    }
  };

  const handleDownload = (report: GeneratedReport) => {
    const blob = new Blob([report.textContent], { type: 'text/plain' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `statement-${report.accountId}-${report.id}.txt`;
    a.click();
    URL.revokeObjectURL(url);
  };

  return (
    <div className="reports-page">
      <h1>Transaction Reports</h1>
      <Link to="/menu">Back to Main Menu</Link>

      <div style={{ margin: '20px 0' }}>
        <button onClick={handleGenerate} disabled={generating}>
          {generating ? 'Generating...' : 'Generate Statements'}
        </button>
      </div>

      {error && <p className="error" style={{ color: 'red' }}>{error}</p>}

      {selectedReport ? (
        <div className="report-detail">
          <button onClick={() => setSelectedReport(null)}>Back to List</button>
          <h2>Statement for Account {selectedReport.accountId}</h2>
          <p>Customer: {selectedReport.customerName}</p>
          <p>Generated: {selectedReport.generatedAt}</p>
          <button onClick={() => handleDownload(selectedReport)}>Download Text</button>
          <pre style={{ background: '#f5f5f5', padding: '10px', whiteSpace: 'pre-wrap' }}>
            {selectedReport.textContent}
          </pre>
        </div>
      ) : (
        <>
          {loading ? (
            <p>Loading reports...</p>
          ) : reports.length === 0 ? (
            <p>No reports generated yet.</p>
          ) : (
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Account</th>
                  <th>Customer</th>
                  <th>Type</th>
                  <th>Generated</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {reports.map((r) => (
                  <tr key={r.id}>
                    <td>{r.id}</td>
                    <td>{r.accountId}</td>
                    <td>{r.customerName}</td>
                    <td>{r.reportType}</td>
                    <td>{r.generatedAt}</td>
                    <td>
                      <button onClick={() => setSelectedReport(r)}>View</button>
                      <button onClick={() => handleDownload(r)}>Download</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </>
      )}
    </div>
  );
}
