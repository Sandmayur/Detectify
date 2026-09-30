import React, { useState } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { Shield, Search, AlertTriangle, CheckCircle, Info, ChevronRight, Briefcase, Mail } from 'lucide-react';
import { analysisApi, type AnalysisRequest, type AnalysisResponse } from './services/analysisApi';
import { AuthProvider } from './contexts/AuthContext';
import { Navbar } from './components/Navbar';
import { ProtectedRoute } from './components/ProtectedRoute';
import { AdminRoute } from './components/AdminRoute';
import { Login } from './pages/Login';
import { Register } from './pages/Register';
import { ReportsPage } from './pages/ReportsPage';
import { SubmitReport } from './pages/SubmitReport';
import { AdminDashboard } from './pages/AdminDashboard';
import { DisputePage } from './pages/DisputePage';
import './index.css';

const Home: React.FC = () => {
    const [identifier, setIdentifier] = useState('');
    const [jobDescription, setJobDescription] = useState('');
    const [recruiterEmail, setRecruiterEmail] = useState('');
    
    const [isLoading, setIsLoading] = useState(false);
    const [result, setResult] = useState<AnalysisResponse | null>(null);
    const [error, setError] = useState<string | null>(null);

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
        <div className="max-w-5xl mx-auto px-4 py-12">
            {!result ? (
                <div className="text-center max-w-3xl mx-auto mb-16">
                    <Shield className="w-16 h-16 text-indigo-600 mx-auto mb-6" />
                    <h1 className="text-4xl sm:text-5xl font-extrabold text-slate-900 tracking-tight mb-6">
                        Don't Get Scammed.<br />
                        <span className="text-indigo-600">Verify Your Next Employer.</span>
                    </h1>
                    <p className="text-lg text-slate-600 mb-10 leading-relaxed">
                        Detectify analyzes job offers, domains, and recruiter patterns instantly using AI and community intelligence to keep you safe from employment fraud.
                    </p>

                    <form onSubmit={handleAnalyze} className="bg-white p-6 sm:p-8 rounded-3xl shadow-xl border border-slate-100 text-left">
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
                </div>
            ) : (
                <div className="max-w-3xl mx-auto">
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
    );
};

const ProtectedDashboard: React.FC = () => (
    <div className="p-8 text-center">
        <h1 className="text-3xl font-bold text-slate-900">Protected Area</h1>
        <p className="mt-4 text-slate-600">You can only see this if you are logged in.</p>
    </div>
);

function App() {
    return (
        <AuthProvider>
            <BrowserRouter>
                <div className="min-h-screen bg-slate-50 font-sans text-slate-900">
                    <Navbar />
                    <main>
                        <Routes>
                            <Route path="/" element={<Home />} />
                            <Route path="/login" element={<Login />} />
                            <Route path="/register" element={<Register />} />
                            <Route path="/reports" element={<ReportsPage />} />
                            <Route path="/dispute" element={<DisputePage />} />
                            
                            {/* Protected Routes */}
                            <Route element={<ProtectedRoute />}>
                                <Route path="/reports/new" element={<SubmitReport />} />
                                <Route path="/protected" element={<ProtectedDashboard />} />
                            </Route>

                            {/* Admin Routes */}
                            <Route element={<AdminRoute />}>
                                <Route path="/admin" element={<AdminDashboard />} />
                            </Route>
                        </Routes>
                    </main>
                </div>
            </BrowserRouter>
        </AuthProvider>
    );
}

export default App;
