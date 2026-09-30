import React, { useEffect, useState } from 'react';
import { adminApi, type AdminUserDto, type DisputeResponse } from '../services/adminApi';
import { type ReportResponse } from '../services/reportApi';
import { Check, X, AlertTriangle, Link as LinkIcon, Mail, Phone, Image as ImageIcon, Users, ShieldAlert, FileText, Ban } from 'lucide-react';

export const AdminDashboard: React.FC = () => {
    const [reports, setReports] = useState<ReportResponse[]>([]);
    const [disputes, setDisputes] = useState<DisputeResponse[]>([]);
    const [users, setUsers] = useState<AdminUserDto[]>([]);
    
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [mainTab, setMainTab] = useState<'REPORTS' | 'DISPUTES' | 'USERS'>('REPORTS');
    const [reportTab, setReportTab] = useState<'PENDING' | 'APPROVED' | 'REJECTED'>('PENDING');

    useEffect(() => {
        loadData();
    }, [mainTab, reportTab]);

    const loadData = async () => {
        try {
            setIsLoading(true);
            if (mainTab === 'REPORTS') {
                const data = await adminApi.getReports(reportTab, 0, 50);
                setReports(data.content);
            } else if (mainTab === 'DISPUTES') {
                const data = await adminApi.getDisputes('PENDING', 0, 50);
                setDisputes(data.content);
            } else if (mainTab === 'USERS') {
                const data = await adminApi.getUsers(0, 50);
                setUsers(data.content);
            }
        } catch (err) {
            setError('Failed to load data.');
        } finally {
            setIsLoading(false);
        }
    };

    const handleUpdateReportStatus = async (reportId: string, newStatus: 'APPROVED' | 'REJECTED') => {
        try {
            await adminApi.updateReportStatus(reportId, newStatus);
            setReports(prev => prev.filter(r => r.id !== reportId));
        } catch (err) {
            alert('Failed to update status');
        }
    };

    const handleResolveDispute = async (disputeId: string, newStatus: 'RESOLVED' | 'REJECTED') => {
        try {
            await adminApi.resolveDispute(disputeId, newStatus);
            setDisputes(prev => prev.filter(d => d.id !== disputeId));
        } catch (err) {
            alert('Failed to resolve dispute');
        }
    };

    const handleSuspendUser = async (userId: string, suspend: boolean) => {
        try {
            await adminApi.suspendUser(userId, suspend);
            setUsers(prev => prev.map(u => u.id === userId ? { ...u, isSuspended: suspend } : u));
        } catch (err) {
            alert('Failed to suspend/unsuspend user');
        }
    };

    return (
        <div className="max-w-6xl mx-auto px-4 py-8">
            <h1 className="text-3xl font-bold text-slate-900 mb-8">Admin Dashboard</h1>

            <div className="flex space-x-4 mb-6 border-b border-slate-200 pb-2">
                <button
                    onClick={() => setMainTab('REPORTS')}
                    className={`flex items-center gap-2 px-4 py-2 font-bold ${mainTab === 'REPORTS' ? 'text-indigo-600 border-b-2 border-indigo-600' : 'text-slate-500 hover:text-slate-700'}`}
                >
                    <FileText className="w-5 h-5" /> Reports
                </button>
                <button
                    onClick={() => setMainTab('DISPUTES')}
                    className={`flex items-center gap-2 px-4 py-2 font-bold ${mainTab === 'DISPUTES' ? 'text-indigo-600 border-b-2 border-indigo-600' : 'text-slate-500 hover:text-slate-700'}`}
                >
                    <ShieldAlert className="w-5 h-5" /> Disputes
                </button>
                <button
                    onClick={() => setMainTab('USERS')}
                    className={`flex items-center gap-2 px-4 py-2 font-bold ${mainTab === 'USERS' ? 'text-indigo-600 border-b-2 border-indigo-600' : 'text-slate-500 hover:text-slate-700'}`}
                >
                    <Users className="w-5 h-5" /> Users
                </button>
            </div>

            {error && (
                <div className="mb-6 p-4 bg-red-50 text-red-700 rounded-xl flex items-start gap-3">
                    <AlertTriangle className="h-5 w-5 shrink-0" />
                    {error}
                </div>
            )}

            {mainTab === 'REPORTS' && (
                <>
                    <div className="flex space-x-4 mb-6">
                        <button
                            onClick={() => setReportTab('PENDING')}
                            className={`px-4 py-2 rounded-lg font-medium ${reportTab === 'PENDING' ? 'bg-indigo-100 text-indigo-700' : 'bg-white text-slate-600 border border-slate-200'}`}
                        >
                            Pending
                        </button>
                        <button
                            onClick={() => setReportTab('APPROVED')}
                            className={`px-4 py-2 rounded-lg font-medium ${reportTab === 'APPROVED' ? 'bg-indigo-100 text-indigo-700' : 'bg-white text-slate-600 border border-slate-200'}`}
                        >
                            Approved
                        </button>
                        <button
                            onClick={() => setReportTab('REJECTED')}
                            className={`px-4 py-2 rounded-lg font-medium ${reportTab === 'REJECTED' ? 'bg-indigo-100 text-indigo-700' : 'bg-white text-slate-600 border border-slate-200'}`}
                        >
                            Rejected
                        </button>
                    </div>

                    {isLoading ? (
                        <div className="flex justify-center py-12"><span className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600"></span></div>
                    ) : reports.length === 0 ? (
                        <div className="text-center py-16 bg-white border border-slate-200 rounded-2xl text-slate-500">No reports found.</div>
                    ) : (
                        <div className="space-y-4">
                            {reports.map(report => (
                                <div key={report.id} className="bg-white p-6 rounded-2xl shadow-sm border border-slate-200">
                                    <div className="flex justify-between items-start">
                                        <div>
                                            <h3 className="text-xl font-bold text-slate-900">{report.companyName}</h3>
                                            <p className="text-sm text-indigo-600">{report.companyDomain}</p>
                                        </div>
                                    </div>
                                    <p className="mt-4 text-slate-700 bg-slate-50 p-4 rounded-xl">{report.description}</p>
                                    
                                    {reportTab === 'PENDING' && (
                                        <div className="mt-6 flex gap-3">
                                            <button onClick={() => handleUpdateReportStatus(report.id, 'REJECTED')} className="px-4 py-2 text-red-600 border border-red-200 rounded-xl hover:bg-red-50">Reject</button>
                                            <button onClick={() => handleUpdateReportStatus(report.id, 'APPROVED')} className="px-4 py-2 bg-emerald-600 text-white rounded-xl hover:bg-emerald-700">Approve</button>
                                        </div>
                                    )}
                                </div>
                            ))}
                        </div>
                    )}
                </>
            )}

            {mainTab === 'DISPUTES' && (
                <>
                    {isLoading ? (
                        <div className="flex justify-center py-12"><span className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600"></span></div>
                    ) : disputes.length === 0 ? (
                        <div className="text-center py-16 bg-white border border-slate-200 rounded-2xl text-slate-500">No pending disputes found.</div>
                    ) : (
                        <div className="space-y-4">
                            {disputes.map(dispute => (
                                <div key={dispute.id} className="bg-white p-6 rounded-2xl shadow-sm border border-slate-200 border-l-4 border-l-amber-500">
                                    <div className="flex justify-between mb-4">
                                        <h3 className="font-bold text-lg">{dispute.companyDomain}</h3>
                                        <span className="text-sm text-slate-500">{new Date(dispute.createdAt).toLocaleString()}</span>
                                    </div>
                                    <div className="text-sm text-slate-600 mb-2"><strong>Contact:</strong> {dispute.contactEmail}</div>
                                    <p className="bg-amber-50 text-amber-900 p-4 rounded-xl">{dispute.reason}</p>
                                    <div className="mt-6 flex gap-3">
                                        <button onClick={() => handleResolveDispute(dispute.id, 'REJECTED')} className="px-4 py-2 text-red-600 border border-red-200 rounded-xl hover:bg-red-50">Reject Dispute</button>
                                        <button onClick={() => handleResolveDispute(dispute.id, 'RESOLVED')} className="px-4 py-2 bg-emerald-600 text-white rounded-xl hover:bg-emerald-700">Mark Resolved</button>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </>
            )}

            {mainTab === 'USERS' && (
                <>
                    {isLoading ? (
                        <div className="flex justify-center py-12"><span className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600"></span></div>
                    ) : users.length === 0 ? (
                        <div className="text-center py-16 bg-white border border-slate-200 rounded-2xl text-slate-500">No users found.</div>
                    ) : (
                        <div className="overflow-hidden bg-white border border-slate-200 rounded-2xl shadow-sm">
                            <table className="w-full text-left">
                                <thead className="bg-slate-50 border-b border-slate-200">
                                    <tr>
                                        <th className="px-6 py-4 font-semibold text-slate-700">Email</th>
                                        <th className="px-6 py-4 font-semibold text-slate-700">Role</th>
                                        <th className="px-6 py-4 font-semibold text-slate-700">Status</th>
                                        <th className="px-6 py-4 font-semibold text-slate-700">Actions</th>
                                    </tr>
                                </thead>
                                <tbody className="divide-y divide-slate-100">
                                    {users.map(user => (
                                        <tr key={user.id}>
                                            <td className="px-6 py-4 font-medium">{user.email}</td>
                                            <td className="px-6 py-4"><span className="px-2.5 py-1 text-xs font-bold rounded-full bg-slate-100 text-slate-700">{user.role}</span></td>
                                            <td className="px-6 py-4">
                                                {user.isSuspended ? (
                                                    <span className="text-red-600 font-bold flex items-center gap-1"><Ban className="w-4 h-4" /> Suspended</span>
                                                ) : (
                                                    <span className="text-emerald-600 font-bold flex items-center gap-1"><Check className="w-4 h-4" /> Active</span>
                                                )}
                                            </td>
                                            <td className="px-6 py-4">
                                                {user.role !== 'ADMIN' && (
                                                    <button
                                                        onClick={() => handleSuspendUser(user.id, !user.isSuspended)}
                                                        className={`px-3 py-1.5 rounded-lg text-sm font-bold transition-colors ${user.isSuspended ? 'bg-emerald-50 text-emerald-700 hover:bg-emerald-100' : 'bg-red-50 text-red-700 hover:bg-red-100'}`}
                                                    >
                                                        {user.isSuspended ? 'Unsuspend' : 'Suspend'}
                                                    </button>
                                                )}
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </>
            )}
        </div>
    );
};
