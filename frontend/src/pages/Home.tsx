import React, { useState, useEffect } from 'react';
import { Shield, Search, AlertTriangle, CheckCircle, Info, ChevronRight, Briefcase, Mail, Activity, BookOpen, AlertOctagon } from 'lucide-react';
import { analysisApi, type AnalysisRequest, type AnalysisResponse } from '../services/analysisApi';
import { reportApi, type ReportResponse } from '../services/reportApi';
import { Link } from 'react-router-dom';

export const Home: React.FC = () => {
    const [identifier, setIdentifier] = useState('');
    const [jobDescription, setJobDescription] = useState('');
    const [recruiterEmail, setRecruiterEmail] = useState('');
    
    const [isLoading, setIsLoading] = useState(false);
    const [result, setResult] = useState<AnalysisResponse | null>(null);
    const [error, setError] = useState<string | null>(null);

    const [recentReports, setRecentReports] = useState<ReportResponse[]>([]);

    useEffect(() => {
        const fetchReports = async () => {
            try {
                // Fetch recent approved reports
                const res = await reportApi.getReports(0, 3);
                setRecentReports(res.content);
            } catch (err) {
                console.error("Failed to fetch reports", err);
            }
        };
        fetchReports();
    }, []);

    const handleAnalyze = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!identifier) return;

        try {
            setIsLoading(true);
            setError(null);
            const req: AnalysisRequest = {
                companyIdentifier: identifier,
                jobDescription,
                recruiterEmail
            };
            const res = await analysisApi.analyze(req);
            setResult(res);
        } catch (err: any) {
            setError(err.response?.data?.message || 'Failed to analyze. Please try again.');
        } finally {
            setIsLoading(false);
        }
    };

    const getRiskColor = (category: string) => {
        switch (category) {
            case 'HIGH': return 'text-red-600 bg-red-50 border-red-200';
            case 'MEDIUM': return 'text-amber-600 bg-amber-50 border-amber-200';
            default: return 'text-emerald-600 bg-emerald-50 border-emerald-200';
        }
    };

    const getRiskIcon = (category: string) => {
        switch (category) {
            case 'HIGH': return <AlertTriangle className="w-10 h-10 text-red-600" />;
            case 'MEDIUM': return <Info className="w-10 h-10 text-amber-600" />;
            default: return <CheckCircle className="w-10 h-10 text-emerald-600" />;
        }
    };

    const getSignalColor = (type: string) => {
        switch (type) {
            case 'RED_FLAG': return 'text-red-700 bg-red-100';
            case 'WARNING': return 'text-amber-700 bg-amber-100';
            case 'POSITIVE': return 'text-emerald-700 bg-emerald-100';
            default: return 'text-slate-700 bg-slate-100';
        }
    };

    return (
        <div className="w-full">
            {/* Hero Section */}
            <div className="bg-white border-b border-slate-200">
                <div className="max-w-5xl mx-auto px-4 py-16 sm:py-24 text-center">
                    {!result ? (
                        <>
                            <Shield className="w-16 h-16 text-indigo-600 mx-auto mb-6" />
                            <h1 className="text-4xl sm:text-6xl font-extrabold text-slate-900 tracking-tight mb-6">
                                Check Before You Apply.
                            </h1>
                            <p className="text-xl text-slate-600 mb-12 leading-relaxed max-w-2xl mx-auto">
                                We analyze job offers, domains, and recruiter patterns instantly using AI and community intelligence to assess risk and protect you from fraudulent employment practices.
                            </p>

                            <form onSubmit={handleAnalyze} className="bg-white p-6 sm:p-8 rounded-3xl shadow-2xl border border-slate-100 text-left max-w-3xl mx-auto">
                                <div className="space-y-5">
                                    <div>
                                        <label className="block text-sm font-semibold text-slate-700 mb-1.5">Company Name or Website URL *</label>
                                        <div className="relative">
                                            <Search className="absolute left-4 top-3.5 h-5 w-5 text-slate-400" />
                                            <input 
                                                type="text" 
                                                value={identifier}
                                                onChange={e => setIdentifier(e.target.value)}
                                                className="pl-12 w-full bg-slate-50 border border-slate-200 rounded-2xl py-3.5 focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-all font-medium"
                                                placeholder="e.g. Acme Corp or acme.com"
                                                required
                                            />
                                        </div>
                                    </div>
                                    
                                    <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                                        <div>
                                            <label className="block text-sm font-medium text-slate-700 mb-1.5">Recruiter Email (Optional)</label>
                                            <div className="relative">
                                                <Mail className="absolute left-4 top-3 h-5 w-5 text-slate-400" />
                                                <input 
                                                    type="email" 
                                                    value={recruiterEmail}
                                                    onChange={e => setRecruiterEmail(e.target.value)}
                                                    className="pl-12 w-full bg-slate-50 border border-slate-200 rounded-xl py-2.5 focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-all"
                                                    placeholder="hr@example.com"
                                                />
                                            </div>
                                        </div>
                                        <div>
                                            <label className="block text-sm font-medium text-slate-700 mb-1.5">Job Description Snippet (Optional)</label>
                                            <div className="relative">
                                                <Briefcase className="absolute left-4 top-3 h-5 w-5 text-slate-400" />
                                                <input 
                                                    type="text" 
                                                    value={jobDescription}
                                                    onChange={e => setJobDescription(e.target.value)}
                                                    className="pl-12 w-full bg-slate-50 border border-slate-200 rounded-xl py-2.5 focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-all"
                                                    placeholder="Paste suspicious text here..."
                                                />
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                {error && (
                                    <div className="mt-5 p-4 bg-red-50 text-red-700 rounded-xl text-sm font-medium border border-red-100 flex items-start gap-2">
                                        <AlertTriangle className="w-5 h-5 shrink-0" />
                                        {error}
                                    </div>
                                )}

                                <button 
                                    type="submit" 
                                    disabled={isLoading || !identifier}
                                    className="mt-6 w-full flex items-center justify-center gap-2 bg-indigo-600 hover:bg-indigo-700 text-white py-4 rounded-2xl font-bold text-lg shadow-md transition-all disabled:opacity-50"
                                >
                                    {isLoading ? (
                                        <span className="w-6 h-6 border-2 border-white/20 border-t-white rounded-full animate-spin"></span>
                                    ) : (
                                        <>
                                            Analyze Risk
                                            <ChevronRight className="w-5 h-5" />
                                        </>
                                    )}
                                </button>
                            </form>
                        </>
                    ) : (
                        <div className="max-w-3xl mx-auto text-left">
                            <button 
                                onClick={() => setResult(null)}
                                className="mb-6 text-sm font-medium text-slate-500 hover:text-slate-900 flex items-center gap-1"
                            >
                                ← Back to search
                            </button>

                            <div className={`p-8 rounded-3xl border-2 text-center mb-8 shadow-sm ${getRiskColor(result.riskCategory)}`}>
                                <div className="flex justify-center mb-4">
                                    {getRiskIcon(result.riskCategory)}
                                </div>
                                <h2 className="text-3xl font-extrabold mb-2">{result.companyName}</h2>
                                <div className="text-xl font-bold mb-1 opacity-90">
                                    {result.riskCategory.replace('_', ' ')}
                                </div>
                                <div className="text-4xl font-black mt-4">
                                    {result.finalScore} <span className="text-xl font-medium opacity-70">Risk Score</span>
                                </div>
                            </div>
                            
                            <p className="text-sm text-slate-500 mb-6 text-center italic">
                                Disclaimer: This score is a risk assessment based on available evidence. It does not definitively declare any entity as fraudulent.
                            </p>

                            <h3 className="text-xl font-bold text-slate-900 mb-4">Analysis Signals</h3>
                            <div className="space-y-4">
                                {result.signals.length === 0 && (
                                    <p className="text-slate-500">No specific signals detected.</p>
                                )}
                                {result.signals.map((signal, idx) => (
                                    <div key={idx} className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm flex flex-col sm:flex-row gap-4">
                                        <div className="shrink-0">
                                            <span className={`px-3 py-1 rounded-full text-xs font-bold uppercase tracking-wider ${getSignalColor(signal.signalType)}`}>
                                                {signal.signalType.replace('_', ' ')}
                                            </span>
                                        </div>
                                        <div>
                                            <div className="flex items-center gap-2 mb-1">
                                                <h4 className="font-bold text-slate-900">{signal.signalName}</h4>
                                                <span className={`text-sm font-semibold ${signal.points > 0 ? 'text-red-500' : signal.points < 0 ? 'text-emerald-500' : 'text-slate-400'}`}>
                                                    {signal.points > 0 ? '+' : ''}{signal.points} pts
                                                </span>
                                            </div>
                                            <p className="text-slate-600 text-sm leading-relaxed mb-2">
                                                {signal.explanation}
                                            </p>
                                            <div className="text-xs font-medium text-slate-400 flex items-center gap-1">
                                                Source: {signal.dataSource}
                                            </div>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </div>
                    )}
                </div>
            </div>

            {/* How It Works Section */}
            <div className="max-w-5xl mx-auto px-4 py-20">
                <div className="text-center mb-16">
                    <h2 className="text-3xl font-bold text-slate-900 mb-4">How Detectify Works</h2>
                    <p className="text-slate-600 max-w-2xl mx-auto">We combine technical domain analysis with community intelligence to provide a comprehensive risk assessment.</p>
                </div>
                
                <div className="grid md:grid-cols-3 gap-8">
                    <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-200">
                        <Activity className="w-10 h-10 text-indigo-600 mb-4" />
                        <h3 className="text-xl font-bold mb-2">Domain Analysis</h3>
                        <p className="text-slate-600">We check if the company's website was recently registered, lacks a proper careers page, or matches known suspicious patterns.</p>
                    </div>
                    <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-200">
                        <BookOpen className="w-10 h-10 text-indigo-600 mb-4" />
                        <h3 className="text-xl font-bold mb-2">Community Reports</h3>
                        <p className="text-slate-600">Users submit reports about their experiences. These reports are moderated and factored into the overall risk score.</p>
                    </div>
                    <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-200">
                        <AlertOctagon className="w-10 h-10 text-indigo-600 mb-4" />
                        <h3 className="text-xl font-bold mb-2">Risk Scoring</h3>
                        <p className="text-slate-600">Based on the collected evidence, we generate a final risk score to help you make an informed decision before applying.</p>
                    </div>
                </div>
                
                <div className="mt-8 bg-indigo-50 border border-indigo-100 rounded-2xl p-6 text-sm text-indigo-800">
                    <strong>Honest Limitation:</strong> Without a paid review API, Detectify cannot check employee reviews or ratings from sites like Glassdoor or AmbitionBox. We only analyze what a company shows on its own website and our own community reports. A scammer can fake a good website just as easily as a real small business might have a poor one. Always do your own research!
                </div>
            </div>

            {/* Safety Tips Section */}
            <div className="bg-slate-900 text-white py-20">
                <div className="max-w-5xl mx-auto px-4">
                    <div className="grid md:grid-cols-2 gap-12 items-center">
                        <div>
                            <h2 className="text-3xl font-bold mb-6">Job Seeker Safety Tips</h2>
                            <ul className="space-y-4">
                                <li className="flex gap-3">
                                    <CheckCircle className="w-6 h-6 text-emerald-400 shrink-0" />
                                    <span><strong>Never pay for a job.</strong> Legitimate employers will never ask for payment, training fees, or equipment deposits upfront.</span>
                                </li>
                                <li className="flex gap-3">
                                    <CheckCircle className="w-6 h-6 text-emerald-400 shrink-0" />
                                    <span><strong>Verify the email domain.</strong> Be cautious if the recruiter uses a free email provider (e.g., Gmail, Yahoo) instead of a corporate domain.</span>
                                </li>
                                <li className="flex gap-3">
                                    <CheckCircle className="w-6 h-6 text-emerald-400 shrink-0" />
                                    <span><strong>Research the company.</strong> Check for a physical address, a working phone number, and a credible online presence.</span>
                                </li>
                            </ul>
                        </div>
                        <div className="bg-slate-800 p-8 rounded-2xl border border-slate-700">
                            <h3 className="text-xl font-bold mb-4">Signal Explanations</h3>
                            <div className="space-y-4 text-sm">
                                <div>
                                    <span className="inline-block px-2 py-1 rounded bg-red-900/50 text-red-400 font-bold mb-1">RED FLAG</span>
                                    <p className="text-slate-400">High probability of risk. Examples include known scam keywords, heavily downvoted community reports, or very recently registered domains.</p>
                                </div>
                                <div>
                                    <span className="inline-block px-2 py-1 rounded bg-amber-900/50 text-amber-400 font-bold mb-1">WARNING</span>
                                    <p className="text-slate-400">Suspicious elements that require further verification, such as missing careers pages or free email addresses.</p>
                                </div>
                                <div>
                                    <span className="inline-block px-2 py-1 rounded bg-emerald-900/50 text-emerald-400 font-bold mb-1">POSITIVE</span>
                                    <p className="text-slate-400">Verified elements that increase trust, such as official company registrations or a long-standing web presence.</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            {/* Recent Community Reports */}
            <div className="max-w-5xl mx-auto px-4 py-20">
                <div className="flex justify-between items-end mb-8">
                    <div>
                        <h2 className="text-3xl font-bold text-slate-900 mb-2">Recent Community Reports</h2>
                        <p className="text-slate-600">Approved submissions from other job seekers (unverified allegations).</p>
                    </div>
                    <Link to="/reports" className="text-indigo-600 font-bold hover:underline hidden sm:block">View all reports →</Link>
                </div>
                
                <div className="grid md:grid-cols-3 gap-6">
                    {recentReports.length === 0 ? (
                        <div className="col-span-3 text-center py-12 text-slate-500 bg-slate-50 rounded-2xl">
                            No recent reports available.
                        </div>
                    ) : (
                        recentReports.map(report => (
                            <div key={report.id} className="bg-white p-6 rounded-2xl shadow-sm border border-slate-200 flex flex-col">
                                <h3 className="font-bold text-lg text-slate-900 truncate">{report.companyName}</h3>
                                <p className="text-sm text-indigo-600 mb-3">{report.companyDomain}</p>
                                <p className="text-slate-600 text-sm line-clamp-3 mb-4 flex-grow">{report.description}</p>
                                <div className="flex justify-between items-center text-xs text-slate-400 pt-4 border-t border-slate-100">
                                    <span>{new Date(report.createdAt).toLocaleDateString()}</span>
                                    <span className="flex items-center gap-1 font-medium text-slate-500">
                                        <AlertTriangle className="w-3 h-3" /> Unverified
                                    </span>
                                </div>
                            </div>
                        ))
                    )}
                </div>
                <div className="mt-8 text-center sm:hidden">
                    <Link to="/reports" className="inline-block bg-slate-100 text-slate-700 font-bold px-6 py-3 rounded-xl">View all reports</Link>
                </div>
            </div>
        </div>
    );
};
