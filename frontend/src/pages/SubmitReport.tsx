import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { reportApi, type SubmitReportRequest } from '../services/reportApi';
import { AlertCircle, ArrowRight, Building, Link as LinkIcon, Mail, Phone, Image as ImageIcon } from 'lucide-react';

const reportSchema = z.object({
    companyName: z.string().min(1, 'Company name is required'),
    companyDomain: z.string().optional(),
    description: z.string().min(20, 'Please provide more details (at least 20 characters)'),
    jobUrl: z.string().url('Must be a valid URL').optional().or(z.literal('')),
    recruiterEmail: z.string().email('Must be a valid email').optional().or(z.literal('')),
    recruiterPhone: z.string().optional().or(z.literal('')),
    evidenceUrl: z.string().url('Must be a valid URL').optional().or(z.literal('')),
}).refine(data => {
    return (data.jobUrl && data.jobUrl.length > 0) || 
           (data.recruiterEmail && data.recruiterEmail.length > 0) || 
           (data.recruiterPhone && data.recruiterPhone.length > 0) || 
           (data.evidenceUrl && data.evidenceUrl.length > 0);
}, {
    message: "At least one piece of evidence is required (URL, Email, Phone, or Evidence Image)",
    path: ["evidenceUrl"] // attach error to one of the fields to display it
});

type ReportForm = z.infer<typeof reportSchema>;

export const SubmitReport: React.FC = () => {
    const navigate = useNavigate();
    const [serverError, setServerError] = useState<string | null>(null);

    const {
        register,
        handleSubmit,
        formState: { errors, isSubmitting },
    } = useForm<ReportForm>({
        resolver: zodResolver(reportSchema),
        defaultValues: {
            companyName: '',
            companyDomain: '',
            description: '',
            jobUrl: '',
            recruiterEmail: '',
            recruiterPhone: '',
            evidenceUrl: ''
        }
    });

    const onSubmit = async (data: ReportForm) => {
        try {
            setServerError(null);
            
            // Clean empty strings
            const payload: SubmitReportRequest = {
                companyName: data.companyName,
                companyDomain: data.companyDomain || undefined,
                description: data.description,
                jobUrl: data.jobUrl || undefined,
                recruiterEmail: data.recruiterEmail || undefined,
                recruiterPhone: data.recruiterPhone || undefined,
                evidenceUrl: data.evidenceUrl || undefined,
            };

            await reportApi.submitReport(payload);
            navigate('/reports', { state: { message: 'Report submitted successfully and is pending review.' } });
        } catch (error: any) {
            if (error.response?.data?.message) {
                setServerError(error.response.data.message);
            } else {
                setServerError('An unexpected error occurred. Please try again.');
            }
        }
    };

    return (
        <div className="max-w-3xl mx-auto px-4 py-8">
            <div className="mb-8">
                <h1 className="text-3xl font-bold text-slate-900">Submit a Report</h1>
                <p className="text-slate-600 mt-2">Share your experience to help others identify potential risks.</p>
            </div>

            <div className="bg-white p-6 sm:p-8 rounded-2xl shadow-sm border border-slate-200">
                <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
                    {serverError && (
                        <div className="p-4 bg-red-50 border border-red-100 rounded-xl flex items-start gap-3">
                            <AlertCircle className="h-5 w-5 text-red-500 shrink-0 mt-0.5" />
                            <p className="text-sm text-red-700 font-medium">{serverError}</p>
                        </div>
                    )}

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <div>
                            <label className="block text-sm font-medium text-slate-700 mb-1">Company Name *</label>
                            <div className="relative">
                                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                                    <Building className="h-5 w-5 text-slate-400" />
                                </div>
                                <input
                                    type="text"
                                    className={`pl-10 block w-full rounded-xl border ${errors.companyName ? 'border-red-300 focus:ring-red-500' : 'border-slate-300 focus:ring-indigo-500'} py-2.5 shadow-sm sm:text-sm`}
                                    placeholder="e.g. Acme Corp"
                                    {...register('companyName')}
                                />
                            </div>
                            {errors.companyName && <p className="mt-1 text-sm text-red-600">{errors.companyName.message}</p>}
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-slate-700 mb-1">Company Domain</label>
                            <div className="relative">
                                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                                    <LinkIcon className="h-5 w-5 text-slate-400" />
                                </div>
                                <input
                                    type="text"
                                    className="pl-10 block w-full rounded-xl border border-slate-300 focus:ring-indigo-500 focus:border-indigo-500 py-2.5 shadow-sm sm:text-sm"
                                    placeholder="e.g. acme-jobs.com"
                                    {...register('companyDomain')}
                                />
                            </div>
                        </div>
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-slate-700 mb-1">Description of the incident *</label>
                        <textarea
                            rows={4}
                            className={`block w-full rounded-xl border ${errors.description ? 'border-red-300 focus:ring-red-500' : 'border-slate-300 focus:ring-indigo-500'} py-2.5 px-3 shadow-sm sm:text-sm`}
                            placeholder="What happened? Did they ask for money? Was the interview process suspicious?"
                            {...register('description')}
                        />
                        {errors.description && <p className="mt-1 text-sm text-red-600">{errors.description.message}</p>}
                    </div>

                    <div className="pt-6 border-t border-slate-200">
                        <h3 className="text-lg font-medium text-slate-900 mb-4">Evidence (Provide at least one)</h3>
                        {errors.evidenceUrl && <p className="mb-4 text-sm font-medium text-red-600 flex items-center gap-1"><AlertCircle className="w-4 h-4"/>{errors.evidenceUrl.message}</p>}
                        
                        <div className="space-y-4">
                            <div>
                                <label className="block text-sm font-medium text-slate-700 mb-1">Job Post URL</label>
                                <div className="relative">
                                    <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                                        <LinkIcon className="h-5 w-5 text-slate-400" />
                                    </div>
                                    <input
                                        type="text"
                                        className="pl-10 block w-full rounded-xl border border-slate-300 focus:ring-indigo-500 focus:border-indigo-500 py-2.5 shadow-sm sm:text-sm"
                                        placeholder="https://..."
                                        {...register('jobUrl')}
                                    />
                                </div>
                                {errors.jobUrl && <p className="mt-1 text-sm text-red-600">{errors.jobUrl.message}</p>}
                            </div>

                            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                                <div>
                                    <label className="block text-sm font-medium text-slate-700 mb-1">Recruiter Email</label>
                                    <div className="relative">
                                        <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                                            <Mail className="h-5 w-5 text-slate-400" />
                                        </div>
                                        <input
                                            type="email"
                                            className="pl-10 block w-full rounded-xl border border-slate-300 focus:ring-indigo-500 focus:border-indigo-500 py-2.5 shadow-sm sm:text-sm"
                                            placeholder="recruiter@example.com"
                                            {...register('recruiterEmail')}
                                        />
                                    </div>
                                    {errors.recruiterEmail && <p className="mt-1 text-sm text-red-600">{errors.recruiterEmail.message}</p>}
                                </div>

                                <div>
                                    <label className="block text-sm font-medium text-slate-700 mb-1">Recruiter Phone</label>
                                    <div className="relative">
                                        <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                                            <Phone className="h-5 w-5 text-slate-400" />
                                        </div>
                                        <input
                                            type="text"
                                            className="pl-10 block w-full rounded-xl border border-slate-300 focus:ring-indigo-500 focus:border-indigo-500 py-2.5 shadow-sm sm:text-sm"
                                            placeholder="+91..."
                                            {...register('recruiterPhone')}
                                        />
                                    </div>
                                </div>
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-slate-700 mb-1">Evidence Image URL (Mocked Upload)</label>
                                <div className="relative">
                                    <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                                        <ImageIcon className="h-5 w-5 text-slate-400" />
                                    </div>
                                    <input
                                        type="text"
                                        className="pl-10 block w-full rounded-xl border border-slate-300 focus:ring-indigo-500 focus:border-indigo-500 py-2.5 shadow-sm sm:text-sm"
                                        placeholder="https://..."
                                        {...register('evidenceUrl')}
                                    />
                                </div>
                            </div>
                        </div>
                    </div>

                    <div className="pt-4">
                        <button
                            type="submit"
                            disabled={isSubmitting}
                            className="w-full flex justify-center items-center gap-2 py-3 px-4 border border-transparent rounded-xl shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50 transition-all"
                        >
                            {isSubmitting ? (
                                <span className="w-5 h-5 border-2 border-white/20 border-t-white rounded-full animate-spin"></span>
                            ) : (
                                <>
                                    Submit Report
                                    <ArrowRight className="w-4 h-4" />
                                </>
                            )}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};
