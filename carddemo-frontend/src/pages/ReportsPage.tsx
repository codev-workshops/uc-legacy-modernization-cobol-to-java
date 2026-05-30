import { useState, type FormEvent } from 'react';
import { useMutation } from '@tanstack/react-query';
import { reportService } from '../services/reportService';
import { DataTable } from '../components/DataTable';
import { FormField } from '../components/FormField';
import type { ReportData, Transaction } from '../types';

export function ReportsPage() {
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');

  const mutation = useMutation({
    mutationFn: reportService.generateReport,
  });

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    mutation.mutate({ startDate, endDate });
  };

  const report: ReportData | undefined = mutation.data;

  return (
    <div>
      <h1 className="mb-6 text-2xl font-bold text-gray-800">Reports</h1>
      <form
        onSubmit={handleSubmit}
        className="mb-6 flex flex-wrap items-end gap-4 rounded-lg bg-white p-6 shadow"
      >
        <FormField
          label="Start Date"
          type="date"
          value={startDate}
          onChange={(e) => setStartDate(e.target.value)}
          required
        />
        <FormField
          label="End Date"
          type="date"
          value={endDate}
          onChange={(e) => setEndDate(e.target.value)}
          required
        />
        <button
          type="submit"
          disabled={mutation.isPending}
          className="mb-4 rounded bg-blue-600 px-4 py-2 text-white hover:bg-blue-700 disabled:opacity-50"
        >
          {mutation.isPending ? 'Generating...' : 'Generate Report'}
        </button>
      </form>

      {mutation.error && (
        <div className="mb-4 rounded bg-red-50 p-3 text-sm text-red-700">
          Failed to generate report
        </div>
      )}

      {report && (
        <>
          <div className="mb-4 grid gap-4 sm:grid-cols-3">
            <div className="rounded-lg bg-white p-4 shadow">
              <p className="text-sm text-gray-500">Total Transactions</p>
              <p className="text-2xl font-bold">{report.transactionCount}</p>
            </div>
            <div className="rounded-lg bg-white p-4 shadow">
              <p className="text-sm text-gray-500">Total Amount</p>
              <p className="text-2xl font-bold">
                ${report.totalAmount.toFixed(2)}
              </p>
            </div>
            <div className="rounded-lg bg-white p-4 shadow">
              <p className="text-sm text-gray-500">Period</p>
              <p className="text-lg font-semibold">
                {report.startDate} — {report.endDate}
              </p>
            </div>
          </div>
          <div className="rounded-lg bg-white shadow">
            <DataTable<Transaction>
              columns={[
                { key: 'id', header: 'ID' },
                { key: 'cardNumber', header: 'Card' },
                { key: 'transactionType', header: 'Type' },
                {
                  key: 'amount',
                  header: 'Amount',
                  render: (t) => `$${t.amount.toFixed(2)}`,
                },
                { key: 'merchantName', header: 'Merchant' },
                { key: 'transactionDate', header: 'Date' },
              ]}
              data={report.transactions}
              keyExtractor={(t) => t.id}
            />
          </div>
        </>
      )}
    </div>
  );
}
