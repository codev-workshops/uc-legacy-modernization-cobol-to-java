export interface User {
  userId: string;
  firstName: string;
  lastName: string;
  userType: string;
}

export interface AuthResponse {
  token: string;
  refreshToken: string;
  user: User;
}

export interface LoginCredentials {
  userId: string;
  password: string;
}

export interface Account {
  id: number;
  accountNumber: string;
  customerName: string;
  status: string;
  currentBalance: number;
  creditLimit: number;
  openDate: string;
  expirationDate: string;
}

export interface Card {
  cardNumber: string;
  accountId: number;
  cardholderName: string;
  status: string;
  expirationDate: string;
  cardType: string;
}

export interface Transaction {
  id: number;
  cardNumber: string;
  transactionType: string;
  amount: number;
  merchantName: string;
  transactionDate: string;
  description: string;
  status: string;
}

export interface TransactionType {
  typeCode: string;
  typeDescription: string;
}

export interface NewTransaction {
  cardNumber: string;
  transactionTypeCode: string;
  amount: number;
  merchantName: string;
  description: string;
}

export interface BillingStatement {
  accountId: number;
  statementDate: string;
  dueDate: string;
  minimumPayment: number;
  totalBalance: number;
  transactions: Transaction[];
}

export interface ReportRequest {
  startDate: string;
  endDate: string;
  transactionType?: string;
}

export interface ReportData {
  transactions: Transaction[];
  totalAmount: number;
  transactionCount: number;
  startDate: string;
  endDate: string;
}

export interface Customer {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  address: string;
  ssn: string;
}

export interface AuthorizationSummary {
  id: number;
  cardNumber: string;
  amount: number;
  merchantName: string;
  status: string;
  authorizationDate: string;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

export interface ApiError {
  message: string;
  status: number;
  errors?: string[];
}
