import React, { useEffect, useState } from 'react';
import { adminApi } from '../services/adminApi';
import { type ReportResponse } from '../services/reportApi';
import { Check, X, AlertTriangle, Link as LinkIcon, Mail, Phone, Image as ImageIcon } from 'lucide-react';

export const AdminDashboard: React.FC = () => {
    const [reports, setReports] = useState<ReportResponse[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [activeTab, setActiveTab] = useState<'PENDING' | 'APPROVED' | 'REJECTED'>('PENDING');

    useEffect(() => {
        loadReports(activeTab);
    }, [activeTab]);

    const loadReports = async (status: 'PENDING' | 'APPROVED' | 'REJECTED') => {
        try {
            setIsLoading(true);
            const data = await adminApi.getReports(status, 0, 50);
            setReports(data.content);
        } catch (err) {
            setError('Failed to load reports.');
        } finally {
            setIsLoading(false);
        }
    };

    const handleUpdateStatus = async (reportId: string, newStatus: 'APPROVED' | 'REJECTED') => {
        try {
            await adminApi.updateReportStatus(reportId, newStatus);
            // Remove from current list if the tab is showing something else
            setReports(prev => prev.filter(r => r.id !== reportId));
        } catch (err) {
            alert('Failed to update status');
        }
    };

    return (
        <div className="max-w-6xl mx-auto px-4 py-8">
            <h1 className="text-3xl font-bold text-slate-900 mb-8">Admin Dashboard</h1>

            <div className="flex space-x-4 mb-6">
                <button
                    onClick={() => setActiveTab('PENDING')}
                    className={`px-4 py-2 rounded-lg font-medium ${activeTab === 'PENDING' ? 'bg-indigo-100 text-indigo-700' : 'bg-white text-slate-600 hover:bg-slate-50 border border-slate-200'}`}
                >
                    Pending Reports
                </button>
                <button
                    onClick={() => setActiveTab('APPROVED')}
                    className={`px-4 py-2 rounded-lg font-medium ${activeTab === 'APPROVED' ? 'bg-indigo-100 text-indigo-700' : 'bg-white text-slate-600 hover:bg-slate-50 border border-slate-200'}`}
                >
                    Approved Reports
                </button>
                <button
                    onClick={() => setActiveTab('REJECTED')}
                    className={`px-4 py-2 rounded-lg font-medium ${activeTab === 'REJECTED' ? 'bg-indigo-100 text-indigo-700' : 'bg-white text-slate-600 hover:bg-slate-50 border border-slate-200'}`}
                >
                    Rejected Reports
                </button>
            </div>

            {error && (
                <div className="mb-6 p-4 bg-red-50 text-red-700 rounded-xl flex items-start gap-3">
                    <AlertTriangle className="h-5 w-5 shrink-0" />
                    {error}
                </div>
            )}

            {isLoading ? (
                <div className="flex justify-center py-12">
                    <span className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600"></span>
                </div>
            ) : reports.length === 0 ? (
                <div className="text-center py-16 bg-white border border-slate-200 rounded-2xl text-slate-500">
                    No {activeTab.toLowerCase()} reports found.
                </div>
            ) : (
                <div className="space-y-4">
                    {reports.map(report => (
                        <div key={report.id} className="bg-white p-6 rounded-2xl shadow-sm border border-slate-200">
                            <div className="flex justify-between items-start">
                                <div>
                                    <h3 className="text-xl font-bold text-slate-900">{report.companyName}</h3>
                                    {report.companyDomain && (
                                        <a href={`https://${report.companyDomain}`} target="_blank" rel="noreferrer" className="text-sm text-indigo-600 hover:underline">
                                            {report.companyDomain}
                                        </a>
                                    )}
                                </div>
                                <span className="text-sm text-slate-500 bg-slate-100 px-3 py-1 rounded-full">
                                    {new Date(report.createdAt).toLocaleString()}
                                </span>
                            </div>

                            <p className="mt-4 text-slate-700 bg-slate-50 p-4 rounded-xl whitespace-pre-wrap">
                                {report.description}
                            </p>

                            <div className="mt-4 flex flex-wrap gap-3">
                                {report.jobUrl && (
                                    <div className="flex items-center gap-1.5 text-sm bg-indigo-50 text-indigo-700 px-3 py-1.5 rounded-lg font-medium border border-indigo-100">
                                        <LinkIcon className="w-4 h-4" /> <a href={report.jobUrl} target="_blank" rel="noreferrer">Job Link</a>
                                    </div>
                                )}
                                {report.recruiterEmail && (
                                    <div className="flex items-center gap-1.5 text-sm bg-indigo-50 text-indigo-700 px-3 py-1.5 rounded-lg font-medium border border-indigo-100">
                                        <Mail className="w-4 h-4" /> {report.recruiterEmail}
                                    </div>
                                )}
                                {report.recruiterPhone && (
                                    <div className="flex items-center gap-1.5 text-sm bg-indigo-50 text-indigo-700 px-3 py-1.5 rounded-lg font-medium border border-indigo-100">
                                        <Phone className="w-4 h-4" /> {report.recruiterPhone}
                                    </div>
                                )}
                                {report.evidenceUrl && (
                                    <div className="flex items-center gap-1.5 text-sm bg-indigo-50 text-indigo-700 px-3 py-1.5 rounded-lg font-medium border border-indigo-100">
                                        <ImageIcon className="w-4 h-4" /> <a href={report.evidenceUrl} target="_blank" rel="noreferrer">Evidence</a>
                                    </div>
                                )}
                            </div>

                            {activeTab === 'PENDING' && (
                                <div className="mt-6 flex justify-end gap-3 pt-4 border-t border-slate-100">
                                    <button 
                                        onClick={() => handleUpdateStatus(report.id, 'REJECTED')}
                                        className="flex items-center gap-2 px-5 py-2.5 bg-white border border-red-200 text-red-600 rounded-xl hover:bg-red-50 font-bold transition-colors"
                                    >
                                        <X className="w-4 h-4" /> Reject
                                    </button>
                                    <button 
                                        onClick={() => handleUpdateStatus(report.id, 'APPROVED')}
                                        className="flex items-center gap-2 px-5 py-2.5 bg-emerald-600 text-white rounded-xl hover:bg-emerald-700 font-bold transition-colors shadow-sm"
                                    >
                                        <Check className="w-4 h-4" /> Approve
                                    </button>
                                </div>
                            )}
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};
