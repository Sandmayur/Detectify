import React, { useEffect, useState } from 'react';
import { reportApi, type ReportResponse } from '../services/reportApi';
import { ReportCard } from '../components/ReportCard';
import { AlertCircle, FileText } from 'lucide-react';
import { Link } from 'react-router-dom';

export const ReportsPage: React.FC = () => {
    const [reports, setReports] = useState<ReportResponse[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        loadReports();
    }, []);

    const loadReports = async () => {
        try {
            setIsLoading(true);
            const data = await reportApi.getReports(0, 20);
            setReports(data.content);
        } catch (err) {
            setError('Failed to load reports. Please try again later.');
        } finally {
            setIsLoading(false);
        }
    };

    const handleVoteUpdated = (updatedReport: ReportResponse) => {
        setReports(prev => prev.map(r => r.id === updatedReport.id ? updatedReport : r));
    };

    return (
        <div className="max-w-4xl mx-auto px-4 py-8">
            <div className="flex justify-between items-end mb-8">
                <div>
                    <h1 className="text-3xl font-bold text-slate-900">Community Reports</h1>
                    <p className="text-slate-600 mt-2">Browse experiences shared by other job seekers.</p>
                </div>
                <Link to="/reports/new" className="bg-indigo-600 hover:bg-indigo-700 text-white px-5 py-2.5 rounded-xl font-medium shadow-sm transition-colors flex items-center gap-2">
                    <FileText className="w-5 h-5" />
                    Submit Report
                </Link>
            </div>

            {error && (
                <div className="mb-6 p-4 bg-red-50 border border-red-100 rounded-xl flex items-start gap-3">
                    <AlertCircle className="h-5 w-5 text-red-500 shrink-0 mt-0.5" />
                    <p className="text-sm text-red-700 font-medium">{error}</p>
                </div>
            )}

            {isLoading ? (
                <div className="flex justify-center py-12">
                    <span className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600"></span>
                </div>
            ) : reports.length === 0 ? (
                <div className="text-center py-16 bg-white border border-slate-200 rounded-2xl">
                    <FileText className="w-12 h-12 text-slate-300 mx-auto mb-4" />
                    <h3 className="text-lg font-medium text-slate-900">No reports found</h3>
                    <p className="text-slate-500 mt-1">Be the first to share an experience.</p>
                </div>
            ) : (
                <div className="space-y-6">
                    {reports.map(report => (
                        <ReportCard key={report.id} report={report} onVoteUpdated={handleVoteUpdated} />
                    ))}
                </div>
            )}
        </div>
    );
};
