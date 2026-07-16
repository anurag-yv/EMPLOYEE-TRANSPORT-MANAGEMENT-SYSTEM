import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { parseJwt } from '../utils/jwt';
import api from '../api';
import { Mail, Lock, Navigation, ShieldCheck, ArrowRight, Loader2 } from 'lucide-react';

const Login = () => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');
        try {
            const res = await api.post('/api/auth/login', { email, password });
            const token = res.data.token;
            localStorage.setItem('token', token);
            if (res.data.refreshToken) {
                localStorage.setItem('refreshToken', res.data.refreshToken);
            }
            const user = parseJwt(token);
            navigate(user.role === 'ADMIN' ? '/admin' : '/dashboard');
        } catch (err) {
            setError(err.response?.data?.message || 'Invalid credentials. Please try again.');
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
                style={{ width: '100%', maxWidth: '420px' }}
            >
                {/* Logo */}
                <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', marginBottom: '2.5rem', textAlign: 'center' }}>
                    <div style={{ width: 64, height: 64, background: 'white', borderRadius: 18, display: 'flex', alignItems: 'center', justifyContent: 'center', marginBottom: '1.25rem', boxShadow: '0 20px 40px rgba(255,255,255,0.08)' }}>
                        <Navigation size={28} color="black" />
                    </div>
                    <h1 style={{ fontSize: '1.875rem', fontWeight: 900, letterSpacing: '-0.03em', marginBottom: '0.375rem' }}>TransitPort</h1>
                    <p style={{ color: '#94a3b8', fontSize: '0.875rem' }}>Enterprise Mobility Management</p>
                </div>

                <div className="glass-card">
                    <h2 style={{ fontSize: '1.125rem', fontWeight: 800, marginBottom: '1.75rem' }}>Sign in to your account</h2>

                    {error && <div className="alert alert-error">{error}</div>}

                    <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1.125rem' }}>
                        <div className="input-group">
                            <label className="input-label">Work Email</label>
                            <div className="input-container">
                                <Mail className="input-icon" size={17} />
                                <input
                                    type="email"
                                    className="uber-input"
                                    value={email}
                                    onChange={(e) => setEmail(e.target.value)}
                                    required
                                />
                            </div>
                        </div>

                        <div className="input-group">
                            <label className="input-label">Password</label>
                            <div className="input-container">
                                <Lock className="input-icon" size={17} />
                                <input
                                    type="password"
                                    className="uber-input"
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                    required
                                />
                            </div>
                        </div>

                        <button
                            type="submit"
                            disabled={loading}
                            className="uber-btn uber-btn-primary"
                            style={{ marginTop: '0.5rem' }}
                        >
                            {loading ? <Loader2 size={18} style={{ animation: 'spin 1s linear infinite' }} /> : <><span>Sign In</span><ArrowRight size={17} /></>}
                        </button>
                    </form>

                    <div style={{ marginTop: '2rem', paddingTop: '1.5rem', borderTop: '1px solid rgba(255,255,255,0.06)', textAlign: 'center' }}>
                        <p style={{ fontSize: '0.875rem', color: '#94a3b8' }}>
                            Don't have an account?{' '}
                            <Link to="/register" style={{ color: '#111827', fontWeight: 700, textDecoration: 'none' }}
                                onMouseEnter={e => e.target.style.textDecoration = 'underline'}
                                onMouseLeave={e => e.target.style.textDecoration = 'none'}
                            >Register now</Link>
                        </p>
                    </div>
                </div>

                <div style={{ marginTop: '2rem', display: 'flex', justifyContent: 'center', opacity: 0.35 }}>
                    <span style={{ fontSize: '0.7rem', fontWeight: 800, textTransform: 'uppercase', letterSpacing: '0.12em', color: '#64748b', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                        <ShieldCheck size={12} /> Secure Access · v2.1.0
                    </span>
                </div>
            </motion.div>
        </div>
    );
};

export default Login;
