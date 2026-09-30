import React, { useState } from 'react';
import { disputeApi } from '../services/disputeApi';
import { Shield, AlertCircle, CheckCircle } from 'lucide-react';

export const DisputePage: React.FC = () => {
    const [companyDomain, setCompanyDomain] = useState('');
    const [contactEmail, setContactEmail] = useState('');
    const [reason, setReason] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [success, setSuccess] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            setIsLoading(true);
            setError(null);
            await disputeApi.submitDispute({
                companyDomain,
                contactEmail,
                reason
            });
            setSuccess(true);
        } catch (err: any) {
            setError(err.response?.data?.message || 'Failed to submit dispute. Please try again.');
        } finally {
            setIsLoading(false);
        }
    };

    if (success) {
        return (
            <div className="max-w-2xl mx-auto px-4 py-16 text-center">
                <CheckCircle className="w-16 h-16 text-emerald-500 mx-auto mb-6" />
                <h1 className="text-3xl font-bold text-slate-900 mb-4">Dispute Submitted Successfully</h1>
                <p className="text-slate-600 mb-8">Our administrative team will review your dispute and take appropriate action shortly. We may reach out to the provided email for further verification.</p>
                <button 
                    onClick={() => {
                        setSuccess(false);
                        setCompanyDomain('');
                        setReason('');
                        setContactEmail('');
                    }}
                    className="text-indigo-600 font-medium hover:underline"
                >
                    Submit another dispute
                </button>
            </div>
        );
    }

    return (
        <div className="max-w-2xl mx-auto px-4 py-12">
            <div className="text-center mb-10">
                <Shield className="w-12 h-12 text-indigo-600 mx-auto mb-4" />
                <h1 className="text-3xl font-bold text-slate-900 mb-4">Dispute a Report or Score</h1>
                <p className="text-slate-600">If you represent a company and believe a report or risk score is inaccurate, you can submit a dispute for administrative review.</p>
            </div>

            <form onSubmit={handleSubmit} className="bg-white p-6 sm:p-8 rounded-2xl shadow-sm border border-slate-200">
                {error && (
                    <div className="mb-6 p-4 bg-red-50 text-red-700 rounded-xl flex items-start gap-3">
                        <AlertCircle className="h-5 w-5 shrink-0" />
                        {error}
                    </div>
                )}
                <div className="space-y-6">
                    <div>
                        <label className="block text-sm font-semibold text-slate-700 mb-2">Company Domain *</label>
                        <input
                            type="text"
                            required
                            value={companyDomain}
                            onChange={(e) => setCompanyDomain(e.target.value)}
                            className="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-3 focus:ring-2 focus:ring-indigo-500"
                            placeholder="e.g. acme.com"
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-semibold text-slate-700 mb-2">Official Contact Email *</label>
                        <input
                            type="email"
                            required
                            value={contactEmail}
                            onChange={(e) => setContactEmail(e.target.value)}
                            className="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-3 focus:ring-2 focus:ring-indigo-500"
                            placeholder="admin@acme.com"
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-semibold text-slate-700 mb-2">Reason for Dispute *</label>
                        <textarea
                            required
                            rows={5}
                            value={reason}
                            onChange={(e) => setReason(e.target.value)}
                            className="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-3 focus:ring-2 focus:ring-indigo-500"
                            placeholder="Please provide details and evidence as to why the report or score is inaccurate."
                        ></textarea>
                    </div>

                    <button
                        type="submit"
                        disabled={isLoading || !companyDomain || !contactEmail || !reason}
                        className="w-full bg-indigo-600 hover:bg-indigo-700 text-white font-bold py-3.5 rounded-xl transition-colors disabled:opacity-50"
                    >
                        {isLoading ? 'Submitting...' : 'Submit Dispute'}
                    </button>
                </div>
            </form>
        </div>
    );
};
