import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../api';
import { Mail, Lock, User, Navigation, CheckCircle2, ArrowRight, Loader2, ShieldCheck } from 'lucide-react';
import { motion } from 'framer-motion';

const Register = () => {
    const [formData, setFormData] = useState({ name: '', email: '', password: '', role: 'EMPLOYEE' });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState(false);
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');
        try {
            // Use different endpoints based on role
            // Note: ADMIN registration goes through /api/auth/admin/create-admin
            // EMPLOYEE and CITIZEN go through /api/auth/register
            const endpoint = formData.role === 'ADMIN' 
                ? '/api/auth/admin/create-admin' 
                : '/api/auth/register';
            
            await api.post(endpoint, { ...formData, email: formData.email.toLowerCase().trim() });
            setSuccess(true);
            setTimeout(() => navigate('/login'), 2500);
        } catch (err) {
            setError(err.response?.data?.message || 'Registration failed. Check if email already exists.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '1.5rem', position: 'relative' }}>
            <div className="uber-bg" />

            <motion.div
                initial={{ opacity: 0, y: 24 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.4 }}
                style={{ width: '100%', maxWidth: '480px' }}
            >
                {/* Logo */}
                <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', marginBottom: '2rem', textAlign: 'center' }}>
                    <div style={{ width: 56, height: 56, background: 'white', borderRadius: 16, display: 'flex', alignItems: 'center', justifyContent: 'center', marginBottom: '1rem', boxShadow: '0 16px 32px rgba(255,255,255,0.06)' }}>
                        <Navigation size={24} color="black" />
                    </div>
                    <h1 style={{ fontSize: '1.5rem', fontWeight: 900, letterSpacing: '-0.03em', marginBottom: '0.25rem' }}>Create Account</h1>
                    <p style={{ color: '#94a3b8', fontSize: '0.875rem' }}>Join the transport network</p>
                </div>

                <div className="glass-card">
                    {success ? (
                        <div style={{ textAlign: 'center', padding: '2.5rem 1rem' }}>
                            <div style={{ width: 72, height: 72, background: 'rgba(16,185,129,0.1)', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 1.5rem', border: '1px solid rgba(16,185,129,0.2)' }}>
                                <CheckCircle2 size={36} style={{ color: '#10b981' }} />
                            </div>
                            <h2 style={{ fontSize: '1.375rem', fontWeight: 800, marginBottom: '0.625rem' }}>All Set!</h2>
                            <p style={{ color: '#94a3b8', fontSize: '0.875rem' }}>Account created. Redirecting to login...</p>
                        </div>
                    ) : (
                        <>
                            <h2 style={{ fontSize: '1.125rem', fontWeight: 800, marginBottom: '1.75rem' }}>Create your account</h2>

                            {error && <div className="alert alert-error">{error}</div>}

                            <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1.125rem' }}>
                                <div className="input-group">
                                    <label className="input-label">Full Name</label>
                                    <div className="input-container">
                                        <User className="input-icon" size={17} />
                                        <input className="uber-input" value={formData.name}
                                            onChange={e => setFormData({ ...formData, name: e.target.value })} required />
                                    </div>
                                </div>

                                <div className="input-group">
                                    <label className="input-label">Work Email</label>
                                    <div className="input-container">
                                        <Mail className="input-icon" size={17} />
                                        <input type="email" className="uber-input"
                                            value={formData.email} onChange={e => setFormData({ ...formData, email: e.target.value })} required />
                                    </div>
                                </div>

                                <div className="input-group">
                                    <label className="input-label">Password</label>
                                    <div className="input-container">
                                        <Lock className="input-icon" size={17} />
                                        <input type="password" className="uber-input"
                                            value={formData.password} onChange={e => setFormData({ ...formData, password: e.target.value })} required />
                                    </div>
                                </div>

                                <div className="input-group">
                                    <label className="input-label">Account Role</label>
                                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '0.75rem' }}>
                                        {['EMPLOYEE', 'ADMIN', 'CITIZEN'].map(role => (
                                            <button
                                                key={role}
                                                type="button"
                                                onClick={() => setFormData({ ...formData, role })}
                                                style={{
                                                    padding: '0.625rem', borderRadius: '12px', fontWeight: 700, fontSize: '0.875rem',
                                                    cursor: 'pointer', transition: 'all 0.2s', fontFamily: 'inherit',
                                                    background: formData.role === role ? '#111827' : 'rgba(0,0,0,0.04)',
                                                    color: formData.role === role ? 'white' : '#64748b',
                                                    border: formData.role === role ? '1px solid #111827' : '1px solid rgba(0,0,0,0.08)',
                                                }}
                                            >
                                                {role.charAt(0) + role.slice(1).toLowerCase()}
                                            </button>
                                        ))}
                                    </div>
                                </div>

                                <button
                                    type="submit"
                                    disabled={loading}
                                    className="uber-btn uber-btn-accent"
                                    style={{ marginTop: '0.5rem' }}
                                >
                                    {loading ? <Loader2 size={18} style={{ animation: 'spin 1s linear infinite' }} /> : <><span>Create Account</span><ArrowRight size={17} /></>}
                                </button>
                            </form>

                            <div style={{ marginTop: '1.75rem', paddingTop: '1.5rem', borderTop: '1px solid rgba(255,255,255,0.06)', textAlign: 'center' }}>
                                <p style={{ fontSize: '0.875rem', color: '#94a3b8' }}>
                                    Already have an account?{' '}
                                    <Link to="/login" style={{ color: '#111827', fontWeight: 700, textDecoration: 'none' }}
                                        onMouseEnter={e => e.target.style.textDecoration = 'underline'}
                                        onMouseLeave={e => e.target.style.textDecoration = 'none'}
                                    >Sign in</Link>
                                </p>
                            </div>
                        </>
                    )}
                </div>
            </motion.div>
        </div>
    );
};

export default Register;