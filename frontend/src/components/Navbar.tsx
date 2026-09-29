import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { ShieldCheck, LogOut, User as UserIcon } from 'lucide-react';

export const Navbar: React.FC = () => {
    const { user, logout } = useAuth();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    return (
        <nav className="bg-white border-b border-slate-200 shadow-sm sticky top-0 z-50">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <div className="flex justify-between h-16">
                    <div className="flex items-center">
                        <Link to="/" className="flex items-center gap-2 group">
                            <div className="p-2 bg-indigo-50 rounded-lg group-hover:bg-indigo-100 transition-colors">
                                <ShieldCheck className="h-6 w-6 text-indigo-600" />
                            </div>
                            <span className="font-bold text-xl text-slate-900 tracking-tight">Detectify</span>
                        </Link>
                    </div>
                    
                    <div className="flex items-center gap-4">
                        {user ? (
                            <>
                                {user.role === 'ADMIN' && (
                                    <Link to="/admin" className="text-sm font-medium text-slate-600 hover:text-indigo-600 transition-colors">
                                        Admin Dashboard
                                    </Link>
                                )}
                                <div className="flex items-center gap-2 pl-4 border-l border-slate-200">
                                    <div className="h-8 w-8 rounded-full bg-slate-100 flex items-center justify-center border border-slate-200">
                                        <UserIcon className="h-4 w-4 text-slate-500" />
                                    </div>
                                    <span className="text-sm font-medium text-slate-700 hidden sm:block">
                                        {user.email}
                                    </span>
                                </div>
                                <button
                                    onClick={handleLogout}
                                    className="p-2 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors ml-2"
                                    title="Logout"
                                >
                                    <LogOut className="h-5 w-5" />
                                </button>
                            </>
                        ) : (
                            <>
                                <Link to="/login" className="text-sm font-medium text-slate-600 hover:text-indigo-600 transition-colors">
                                    Log in
                                </Link>
                                <Link to="/register" className="text-sm font-medium bg-indigo-600 text-white px-4 py-2 rounded-lg hover:bg-indigo-700 shadow-sm hover:shadow transition-all">
                                    Sign up
                                </Link>
                            </>
                        )}
                    </div>
                </div>
            </div>
        </nav>
    );
};
