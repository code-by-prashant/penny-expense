export interface Expense {
  id: number;
  date: string;
  amount: number;
  vendorName: string;
  description: string;
  category: string;
  isAnomaly: boolean;
  createdAt: string;
}

export interface ExpenseRequest {
  date: string;
  amount: number;
  vendorName: string;
  description?: string;
}

export interface VendorStat {
  vendorName: string;
  total: number;
  count: number;
}

export interface CategoryStat {
  category: string;
  total: number;
  count: number;
}

export interface DashboardResponse {
  monthlyByCategory: Record<string, Record<string, number>>;
  topVendors: VendorStat[];
  categoryTotals: CategoryStat[];
  anomalies: Expense[];
  anomalyCount: number;
}

export interface CsvUploadResult {
  added: number;
  failed: number;
  errors: string[];
}

export type Tab = 'dashboard' | 'expenses' | 'add' | 'upload';
export type Category = 'Food' | 'Transport' | 'Shopping' | 'Entertainment' | 'Utilities' | 'Health' | 'Finance' | 'Other';
