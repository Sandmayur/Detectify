import React, { useState } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { api } from '../services/api';
import { useNavigate } from 'react-router-dom';
import { AlertTriangle, Lock, Trash2, CheckCircle } from 'lucide-react';

export const Account: React.FC = () => {
    const { user, logout } = useAuth();
    const navigate = useNavigate();

    const [oldPassword, setOldPassword] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    
    const [pwError, setPwError] = useState('');
    const [pwSuccess, setPwSuccess] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);

    const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
    const [deleteError, setDeleteError] = useState('');

    const handlePasswordChange = async (e: React.FormEvent) => {
        e.preventDefault();
        setPwError('');
        setPwSuccess('');

        if (newPassword !== confirmPassword) {
            setPwError('New passwords do not match.');
            return;
        }
        if (newPassword.length < 8) {
            setPwError('Password must be at least 8 characters long.');
            return;
        }

        try {
            setIsSubmitting(true);
            await api.post('/api/users/me/password', { oldPassword, newPassword });
            setPwSuccess('Password changed successfully.');
            setOldPassword('');
            setNewPassword('');
            setConfirmPassword('');
        } catch (err: any) {
            setPwError(err.response?.data?.message || 'Failed to change password.');
        } finally {
            setIsSubmitting(false);
        }
    };

    const handleDeleteAccount = async () => {
        try {
            await api.delete('/api/users/me');
            logout();
            navigate('/');
        } catch (err: any) {
            setDeleteError(err.response?.data?.message || 'Failed to delete account.');
            setShowDeleteConfirm(false);
        }
    };

    if (!user) {
        return null; // ProtectedRoute will handle redirect
    }

    return (
        <div className="max-w-3xl mx-auto px-4 py-12">
            <h1 className="text-3xl font-bold text-slate-900 mb-8">Account Settings</h1>
            
            <div className="bg-white p-6 sm:p-8 rounded-2xl shadow-sm border border-slate-200 mb-8">
                <div className="flex items-center gap-3 mb-6">
                    <div className="w-12 h-12 bg-indigo-100 text-indigo-700 rounded-full flex items-center justify-center font-bold text-xl uppercase">
                        {user.email.charAt(0)}
                    </div>
                    <div>
                        <h2 className="text-xl font-bold text-slate-900">{user.email}</h2>
                        <span className="text-sm font-medium text-slate-500 bg-slate-100 px-2 py-0.5 rounded-full">{user.role}</span>
                    </div>
                </div>
            </div>

            <div className="bg-white p-6 sm:p-8 rounded-2xl shadow-sm border border-slate-200 mb-8">
                <div className="flex items-center gap-2 mb-6">
                    <Lock className="w-6 h-6 text-slate-700" />
                    <h2 className="text-2xl font-bold text-slate-900">Change Password</h2>
                </div>

                <form onSubmit={handlePasswordChange} className="space-y-4 max-w-md">
                    <div>
                        <label className="block text-sm font-medium text-slate-700 mb-1">Current Password</label>
                        <input 
                            type="password" 
                            required 
                            className="w-full bg-slate-50 border border-slate-200 rounded-xl py-2 px-3 focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500" 
                            value={oldPassword} 
                            onChange={e => setOldPassword(e.target.value)} 
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-slate-700 mb-1">New Password</label>
                        <input 
                            type="password" 
                            required 
                            className="w-full bg-slate-50 border border-slate-200 rounded-xl py-2 px-3 focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500" 
                            value={newPassword} 
                            onChange={e => setNewPassword(e.target.value)} 
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-slate-700 mb-1">Confirm New Password</label>
                        <input 
                            type="password" 
                            required 
                            className="w-full bg-slate-50 border border-slate-200 rounded-xl py-2 px-3 focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500" 
                            value={confirmPassword} 
                            onChange={e => setConfirmPassword(e.target.value)} 
                        />
                    </div>

                    {pwError && (
                        <div className="p-3 bg-red-50 text-red-700 rounded-xl text-sm font-medium flex items-center gap-2">
                            <AlertTriangle className="w-4 h-4 shrink-0" />
                            {pwError}
                        </div>
                    )}
                    {pwSuccess && (
                        <div className="p-3 bg-emerald-50 text-emerald-700 rounded-xl text-sm font-medium flex items-center gap-2">
                            <CheckCircle className="w-4 h-4 shrink-0" />
                            {pwSuccess}
                        </div>
                    )}

                    <button 
                        type="submit" 
                        disabled={isSubmitting}
                        className="bg-indigo-600 hover:bg-indigo-700 text-white font-bold py-2 px-4 rounded-xl transition-colors disabled:opacity-50"
                    >
                        Update Password
                    </button>
                </form>
            </div>

            <div className="bg-red-50 p-6 sm:p-8 rounded-2xl border border-red-200">
                <div className="flex items-center gap-2 mb-4">
                    <Trash2 className="w-6 h-6 text-red-700" />
                    <h2 className="text-2xl font-bold text-red-900">Delete Account</h2>
                </div>
                <p className="text-red-800 mb-6 max-w-2xl">
                    Once you delete your account, there is no going back. All your personal data will be permanently deleted. Any reports you have submitted will be anonymized to protect your identity but will remain in the system.
                </p>

                {deleteError && (
                    <div className="mb-4 p-3 bg-white text-red-700 rounded-xl text-sm font-medium border border-red-200 flex items-center gap-2">
                        <AlertTriangle className="w-4 h-4 shrink-0" />
                        {deleteError}
                    </div>
                )}

                {!showDeleteConfirm ? (
                    <button 
                        onClick={() => setShowDeleteConfirm(true)}
                        className="bg-red-600 hover:bg-red-700 text-white font-bold py-2 px-4 rounded-xl transition-colors"
                    >
                        Delete My Account
                    </button>
                ) : (
                    <div className="bg-white p-4 rounded-xl border border-red-200">
                        <p className="font-bold text-slate-900 mb-4">Are you absolutely sure?</p>
                        <div className="flex gap-3">
                            <button 
                                onClick={handleDeleteAccount}
                                className="bg-red-600 hover:bg-red-700 text-white font-bold py-2 px-4 rounded-xl transition-colors"
                            >
                                Yes, Delete My Account
                            </button>
                            <button 
                                onClick={() => setShowDeleteConfirm(false)}
                                className="bg-slate-200 hover:bg-slate-300 text-slate-800 font-bold py-2 px-4 rounded-xl transition-colors"
                            >
                                Cancel
                            </button>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};
