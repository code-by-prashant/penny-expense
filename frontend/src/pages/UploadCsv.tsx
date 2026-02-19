import React, { useRef, useState } from 'react';
import toast from 'react-hot-toast';
import { Upload, FileText, CheckCircle, AlertCircle, Download } from 'lucide-react';
import { api } from '../api/client';
import { Card, Button } from '../components/ui';
import type { CsvUploadResult } from '../types';

const SAMPLE_CSV = `date,amount,vendor_name,description
2024-01-10,350.00,Swiggy,Dinner
2024-01-11,2500.00,Amazon,Electronics
2024-01-12,180.00,Uber,Office commute
2024-01-13,999.00,Netflix,Monthly subscription
2024-01-14,450.00,Airtel,Mobile recharge
2024-01-15,75000.00,Amazon,Laptop (anomaly test)
2024-01-16,1200.00,Zomato,Team lunch
2024-01-17,300.00,Ola,Airport drop
2024-01-18,599.00,Spotify,Annual plan
2024-01-19,850.00,1mg,Medicines`;

function downloadSample() {
  const a = document.createElement('a');
  a.href = 'data:text/csv,' + encodeURIComponent(SAMPLE_CSV);
  a.download = 'sample_expenses.csv';
  a.click();
}

export const UploadCsv: React.FC<{ onSuccess: () => void }> = ({ onSuccess }) => {
  const inputRef = useRef<HTMLInputElement>(null);
  const [file, setFile] = useState<File | null>(null);
  const [uploading, setUploading] = useState(false);
  const [result, setResult] = useState<CsvUploadResult | null>(null);
  const [dragOver, setDragOver] = useState(false);

  const handleFile = (f: File) => { setFile(f); setResult(null); };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault(); setDragOver(false);
    const f = e.dataTransfer.files[0];
    if (f?.name.endsWith('.csv')) handleFile(f);
    else toast.error('Please drop a .csv file');
  };

  const handleUpload = async () => {
    if (!file) return;
    setUploading(true);
    try {
      const res = await api.uploadCsv(file);
      setResult(res);
      toast.success(`Imported ${res.added} expenses`);
      if (res.added > 0) onSuccess();
    } catch {
      toast.error('Upload failed — check that the backend is running');
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="max-w-xl mx-auto space-y-5">
      <Card title="Upload CSV">
        {/* Drop zone */}
        <div
          onDragOver={e => { e.preventDefault(); setDragOver(true); }}
          onDragLeave={() => setDragOver(false)}
          onDrop={handleDrop}
          onClick={() => inputRef.current?.click()}
          className={`border-2 border-dashed rounded-2xl p-10 text-center cursor-pointer transition-all ${
            dragOver ? 'border-lime-400 bg-lime-400/5' : 'border-slate-700 hover:border-slate-500 bg-slate-800/40'
          }`}
        >
          <input ref={inputRef} type="file" accept=".csv"
            onChange={e => e.target.files?.[0] && handleFile(e.target.files[0])}
            className="hidden" />
          <Upload className="mx-auto mb-3 text-slate-500 w-8 h-8" />
          <p className="font-semibold text-slate-300 mb-1">Drop your CSV here</p>
          <p className="text-slate-500 text-sm">or click to browse</p>
        </div>

        {/* File info */}
        {file && (
          <div className="flex items-center gap-3 p-3 rounded-xl bg-slate-800 border border-slate-700 mt-3">
            <FileText className="w-4 h-4 text-lime-400" />
            <div className="flex-1 min-w-0">
              <p className="text-sm font-medium text-slate-200 truncate">{file.name}</p>
              <p className="text-xs text-slate-500">{(file.size / 1024).toFixed(1)} KB</p>
            </div>
          </div>
        )}

        <div className="flex gap-3 mt-4">
          <Button onClick={handleUpload} disabled={!file || uploading}>
            <Upload className="w-4 h-4" />
            {uploading ? 'Importing…' : 'Import'}
          </Button>
        </div>

        {/* Result */}
        {result && (
          <div className={`mt-4 p-4 rounded-xl border ${result.added > 0 ? 'bg-lime-400/5 border-lime-400/15' : 'bg-slate-800 border-slate-700'}`}>
            <div className="flex items-center gap-2 mb-2">
              {result.added > 0 ? <CheckCircle className="w-4 h-4 text-lime-400" /> : <AlertCircle className="w-4 h-4 text-slate-400" />}
              <span className="font-semibold text-sm text-slate-200">
                {result.added} imported · {result.failed} failed
              </span>
            </div>
            {result.errors.length > 0 && (
              <ul className="space-y-1 mt-2">
                {result.errors.slice(0, 5).map((err, i) => (
                  <li key={i} className="text-red-400 text-xs font-mono">· {err}</li>
                ))}
                {result.errors.length > 5 && <li className="text-slate-500 text-xs">+{result.errors.length - 5} more errors</li>}
              </ul>
            )}
          </div>
        )}
      </Card>

      {/* Format guide */}
      <Card title="Expected CSV Format">
        <div className="bg-slate-950 rounded-xl p-4 font-mono text-xs text-lime-400 overflow-x-auto mb-4">
          {`date,amount,vendor_name,description\n2024-01-15,450.00,Swiggy,Lunch order\n2024-01-16,1200.00,Amazon,Books\n2024-01-17,250.00,Uber,Commute`}
        </div>
        <div className="flex items-center gap-3">
          <Button variant="ghost" onClick={downloadSample} className="!text-xs !py-2">
            <Download className="w-3.5 h-3.5" />
            Download Sample CSV
          </Button>
          <p className="text-slate-500 text-xs">Includes an anomaly test row</p>
        </div>
      </Card>
    </div>
  );
};
