import apiClient from './client';

export interface Transaction {
  tranId: string;
  tranTypeCd: string;
  tranCatCd: number;
  tranSource: string;
  tranDesc: string;
  tranAmt: number;
  tranMerchantId: number | null;
  tranMerchantName: string;
  tranMerchantCity: string;
  tranMerchantZip: string;
  tranCardNum: string;
  tranOrigTs: string;
  tranProcTs: string;
}

export interface TransactionPage {
  content: Transaction[];
  totalPages: number;
  totalElements: number;
  number: number;
  size: number;
}

export interface TransactionCreatePayload {
  cardNum: string;
  tranTypeCd: string;
  tranCatCd: number;
  tranAmt: number;
  tranMerchantName: string;
  tranMerchantCity: string;
  tranMerchantZip: string;
  tranSource: string;
  tranDesc: string;
}

export interface BillPaymentPayload {
  cardNum: string;
  amount: number;
}

export async function getTransactions(acctId: number, page = 0, size = 10): Promise<TransactionPage> {
  const { data } = await apiClient.get<TransactionPage>('/transactions', {
    params: { acctId, page, size },
  });
  return data;
}

export async function getTransaction(id: string): Promise<Transaction> {
  const { data } = await apiClient.get<Transaction>(`/transactions/${id}`);
  return data;
}

export async function createTransaction(payload: TransactionCreatePayload): Promise<Transaction> {
  const { data } = await apiClient.post<Transaction>('/transactions', payload);
  return data;
}

export async function payBill(payload: BillPaymentPayload): Promise<Transaction> {
  const { data } = await apiClient.post<Transaction>('/billing/pay', payload);
  return data;
}
