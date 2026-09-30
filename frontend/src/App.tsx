import React, { useEffect, useState } from 'react';
import { BrowserRouter, Routes, Route, Link } from 'react-router-dom';
import { AuthProvider } from './contexts/AuthContext';
import { Navbar } from './components/Navbar';
import { ProtectedRoute } from './components/ProtectedRoute';
import { AdminRoute } from './components/AdminRoute';
import { Login } from './pages/Login';
import { Register } from './pages/Register';
import { ReportsPage } from './pages/ReportsPage';
import { SubmitReport } from './pages/SubmitReport';
import { api } from './services/api';
import './index.css';

const Home: React.FC = () => {
    const [pingStatus, setPingStatus] = useState<string>('Pinging backend...');

    useEffect(() => {
        api.get('/api/ping')
            .then(response => {
                if (response.data.success) {
                    setPingStatus('Backend is UP: ' + response.data.message);
                } else {
                    setPingStatus('Backend responded with unexpected format.');
                }
            })
            .catch(error => {
                setPingStatus('Backend is DOWN or unreachable: ' + error.message);
            });
    }, []);

    return (
        <div className="min-h-[calc(100vh-4rem)] flex flex-col items-center justify-center p-4 bg-slate-50">
            <h1 className="text-4xl font-bold mb-4 text-slate-900">Fake Company Detector</h1>
            <p className="text-xl mb-8 text-slate-600">Phase 3: Authentication</p>

            <div className="bg-white p-6 rounded-2xl shadow-xl shadow-slate-200/50 border border-slate-100 w-full max-w-md text-center">
                <h2 className="text-lg font-semibold mb-4 text-slate-800">Public Dashboard</h2>
                <div className={`p-4 rounded-xl ${pingStatus.includes('UP') ? 'bg-green-50 text-green-700 border border-green-200' : 'bg-red-50 text-red-700 border border-red-200'}`}>
                    {pingStatus}
                </div>
                <div className="mt-8 space-y-4">
                    <Link to="/protected" className="block w-full py-2.5 px-4 bg-indigo-50 text-indigo-700 font-medium rounded-xl hover:bg-indigo-100 transition-colors">
                        Test Protected Route
                    </Link>
                    <Link to="/admin" className="block w-full py-2.5 px-4 bg-slate-50 text-slate-700 font-medium rounded-xl hover:bg-slate-100 transition-colors">
                        Test Admin Route
                    </Link>
                </div>
            </div>
        </div>
    );
};

const ProtectedDashboard: React.FC = () => (
    <div className="p-8 text-center">
        <h1 className="text-3xl font-bold text-slate-900">Protected Area</h1>
        <p className="mt-4 text-slate-600">You can only see this if you are logged in.</p>
    </div>
);

const AdminDashboard: React.FC = () => (
    <div className="p-8 text-center">
        <h1 className="text-3xl font-bold text-slate-900">Admin Dashboard</h1>
        <p className="mt-4 text-slate-600">You can only see this if you are an ADMIN.</p>
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
