import type {
  User,
  Account,
  Card,
  Transaction,
  TransactionType,
  BillingStatement,
  ReportData,
  PaginatedResponse,
} from '../types';

export const mockUser: User = {
  userId: 'admin01',
  firstName: 'John',
  lastName: 'Admin',
  userType: 'ADMIN',
};

export const mockUsers: User[] = [
  mockUser,
  { userId: 'user01', firstName: 'Jane', lastName: 'Doe', userType: 'USER' },
  { userId: 'user02', firstName: 'Bob', lastName: 'Smith', userType: 'USER' },
];

export const mockAccount: Account = {
  id: 1,
  accountNumber: '00000000001',
  customerName: 'John Doe',
  status: 'ACTIVE',
  currentBalance: 5432.10,
  creditLimit: 10000.00,
  openDate: '2020-01-15',
  expirationDate: '2025-01-15',
};

export const mockCards: Card[] = [
  {
    cardNumber: '4111111111111111',
    accountId: 1,
    cardholderName: 'John Doe',
    status: 'ACTIVE',
    expirationDate: '2025-12-31',
    cardType: 'VISA',
  },
  {
    cardNumber: '5500000000000004',
    accountId: 2,
    cardholderName: 'Jane Doe',
    status: 'ACTIVE',
    expirationDate: '2026-06-30',
    cardType: 'MASTERCARD',
  },
];

export const mockTransactions: Transaction[] = Array.from({ length: 25 }, (_, i) => ({
  id: i + 1,
  cardNumber: '4111111111111111',
  transactionType: i % 2 === 0 ? 'PURCHASE' : 'PAYMENT',
  amount: parseFloat((Math.random() * 500 + 10).toFixed(2)),
  merchantName: `Merchant ${i + 1}`,
  transactionDate: `2024-0${(i % 9) + 1}-${String((i % 28) + 1).padStart(2, '0')}`,
  description: `Transaction ${i + 1}`,
  status: 'COMPLETED',
}));

export const mockTransactionPage: PaginatedResponse<Transaction> = {
  content: mockTransactions.slice(0, 20),
  totalElements: 25,
  totalPages: 2,
  page: 0,
  size: 20,
};

export const mockTransactionTypes: TransactionType[] = [
  { typeCode: 'PUR', typeDescription: 'Purchase' },
  { typeCode: 'PAY', typeDescription: 'Payment' },
  { typeCode: 'REF', typeDescription: 'Refund' },
  { typeCode: 'FEE', typeDescription: 'Fee' },
];

export const mockBilling: BillingStatement = {
  accountId: 1,
  statementDate: '2024-09-01',
  dueDate: '2024-09-25',
  minimumPayment: 50.00,
  totalBalance: 5432.10,
  transactions: mockTransactions.slice(0, 5),
};

export const mockReport: ReportData = {
  transactions: mockTransactions.slice(0, 10),
  totalAmount: 2500.00,
  transactionCount: 10,
  startDate: '2024-01-01',
  endDate: '2024-09-30',
};
