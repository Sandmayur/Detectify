import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
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
import { PrivacyPolicy } from './pages/PrivacyPolicy';
import { TermsOfService } from './pages/TermsOfService';
import { Disclaimer } from './pages/Disclaimer';
import { Account } from './pages/Account';
import { Home } from './pages/Home';
import { Footer } from './components/Footer';
import './index.css';



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
                            <Route path="/privacy" element={<PrivacyPolicy />} />
                            <Route path="/terms" element={<TermsOfService />} />
                            <Route path="/disclaimer" element={<Disclaimer />} />
                            
                            {/* Protected Routes */}
                            <Route element={<ProtectedRoute />}>
                                <Route path="/reports/new" element={<SubmitReport />} />
                                <Route path="/account" element={<Account />} />
                                <Route path="/protected" element={<ProtectedDashboard />} />
                            </Route>

                            {/* Admin Routes */}
                            <Route element={<AdminRoute />}>
                                <Route path="/admin" element={<AdminDashboard />} />
                            </Route>
                        </Routes>
                    </main>
                    <Footer />
                </div>
            </BrowserRouter>
        </AuthProvider>
    );
}

export default App;
