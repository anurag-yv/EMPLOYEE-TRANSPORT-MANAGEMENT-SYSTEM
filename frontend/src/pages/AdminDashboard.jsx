import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api';
import {
    LayoutDashboard, Bus, Users, LogOut, Plus,
    Trash2, MapPin, Navigation, Clock, Shield,
    Settings, Menu, Edit2, RefreshCw, Activity,
    ShieldCheck, Database, Zap, X, BarChart3, AlertCircle
} from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';

const AdminDashboard = () => {
    const [routes, setRoutes] = useState([]);
    const [employees, setEmployees] = useState([]);
    const [newRoute, setNewRoute] = useState({ source: '', destination: '', pickupTime: '', capacity: 40 });
    const [loading, setLoading] = useState(true);
    const [activeTab, setActiveTab] = useState('overview');
    const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
    const [showAddModal, setShowAddModal] = useState(false);
    const [alerts, setAlerts] = useState([]);
    const [deleteLoading, setDeleteLoading] = useState(null);
    const [resolveLoading, setResolveLoading] = useState(null);
    const [toast, setToast] = useState(null);
    const navigate = useNavigate();

    useEffect(() => {
        const token = localStorage.getItem('token');
        if (!token) { navigate('/login'); return; }
        fetchData();
        const poll = setInterval(fetchData, 10000);
        return () => clearInterval(poll);
    }, [navigate]);

    const showToast = (msg, type = 'success') => {
        setToast({ msg, type });
        setTimeout(() => setToast(null), 3000);
    };

    const fetchData = async () => {
        setLoading(true);
        try {
            const [r, e, a] = await Promise.all([
                api.get('/api/routes'), 
                api.get('/api/employees'),
                api.get('/api/alerts/active')
            ]);
            setRoutes(Array.isArray(r.data) ? r.data : []);
            setEmployees(Array.isArray(e.data) ? e.data : []);
            setAlerts(Array.isArray(a.data) ? a.data : []);
        } catch (err) {
            console.error(err);
            if (err.response?.status === 403) { localStorage.removeItem('token'); navigate('/login'); }
        } finally { setLoading(false); }
    };

    const handleResolveAlert = async (id) => {
        setResolveLoading(id);
        try {
            await api.put(`/api/alerts/${id}/resolve`);
            await fetchData();
            showToast('Alert resolved.');
        } catch (err) {
            showToast('Failed to resolve alert.', 'error');
        } finally {
            setResolveLoading(null);
        }
    };

    const handleAddRoute = async (e) => {
        e.preventDefault();
        try {
            await api.post('/api/routes', { ...newRoute, bookedSeats: 0 });
            setNewRoute({ source: '', destination: '', pickupTime: '', capacity: 40 });
            setShowAddModal(false);
            await fetchData();
            showToast('Route deployed successfully!');
        } catch (err) { showToast('Error adding route. Please try again.', 'error'); }
    };

    const handleDeleteRoute = async (id) => {
        if (!window.confirm('Delete this route?')) return;
        setDeleteLoading(id);
        try {
            await api.delete(`/api/routes/${id}`);
            await fetchData();
            showToast('Route deleted.');
        } catch (err) { showToast('Delete failed.', 'error'); }
        finally { setDeleteLoading(null); }
    };

    const navItems = [
        { id: 'overview',   label: 'Command Center', icon: LayoutDashboard },
        { id: 'fleet',      label: 'Fleet Manager',  icon: Bus },
        { id: 'personnel',  label: 'Employees',       icon: Users },
        { id: 'config',     label: 'Settings',        icon: Settings },
    ];

    const totalBooked = routes.reduce((s, r) => s + (r.bookedSeats || 0), 0);
    const totalCapacity = routes.reduce((s, r) => s + (r.capacity || 0), 0);

    const SidebarContent = ({ closeMobile }) => (
        <div className="sidebar-inner">
            <div className="sidebar-logo">
                <div className="sidebar-logo-icon accent-bg">
                    <Shield size={20} />
                </div>
                <span className="sidebar-logo-text">AdminHub</span>
            </div>
            <nav className="sidebar-nav">
                {navItems.map(item => (
                    <button
                        key={item.id}
                        onClick={() => { setActiveTab(item.id); closeMobile && closeMobile(); }}
                        className={`nav-btn ${activeTab === item.id ? 'active' : ''}`}
                    >
                        <item.icon size={17} />
                        <span>{item.label}</span>
                    </button>
                ))}
            </nav>
            <div className="sidebar-footer">
                <button
                    onClick={() => { localStorage.removeItem('token'); navigate('/login'); }}
                    className="logout-btn"
                >
                    <LogOut size={17} />
                    <span>Sign Out</span>
                </button>
            </div>
        </div>
    );

    return (
        <div className="dashboard-layout">
            <div className="uber-bg" />

            {/* Toast */}
            <AnimatePresence>
                {toast && (
                    <motion.div
                        initial={{ opacity: 0, y: -20 }}
                        animate={{ opacity: 1, y: 0 }}
                        exit={{ opacity: 0, y: -20 }}
                        style={{
                            position: 'fixed', top: '1rem', right: '1rem', zIndex: 999,
                            padding: '0.875rem 1.25rem', borderRadius: '12px',
                            background: toast.type === 'error' ? 'rgba(244,63,94,0.12)' : 'rgba(16,185,129,0.12)',
                            border: `1px solid ${toast.type === 'error' ? 'rgba(244,63,94,0.3)' : 'rgba(16,185,129,0.3)'}`,
                            color: toast.type === 'error' ? '#f43f5e' : '#10b981',
                            fontWeight: 700, fontSize: '0.875rem'
                        }}
                    >
                        {toast.msg}
                    </motion.div>
                )}
            </AnimatePresence>

            {/* Desktop Sidebar */}
            <aside className="sidebar">
                <SidebarContent />
            </aside>

            {/* Mobile Header */}
            <header className="mobile-header">
                <div className="mobile-header-logo">
                    <Shield size={16} style={{ color: '#6366f1' }} />
                    AdminHub
                </div>
                <button className="menu-toggle-btn" onClick={() => setIsMobileMenuOpen(true)}>
                    <Menu size={20} />
                </button>
            </header>

            {/* Mobile Overlay */}
            <div className={`mobile-overlay ${isMobileMenuOpen ? 'open' : ''}`}>
                <div className="mobile-overlay-bg" onClick={() => setIsMobileMenuOpen(false)} />
                <div className="mobile-sidebar">
                    <SidebarContent closeMobile={() => setIsMobileMenuOpen(false)} />
                </div>
            </div>

            {/* Main */}
            <main className="main-content">
                <div className="page-container">
                    <div className="page-header">
                        <div>
                            <h1 className="page-title">
                                {navItems.find(n => n.id === activeTab)?.label || 'Command Center'}
                            </h1>
                            <p className="page-subtitle">System-wide transport oversight</p>
                        </div>
                        <div className="header-actions">
                            <button className="btn-icon" onClick={fetchData}>
                                <RefreshCw size={17} style={loading ? { animation: 'spin 1s linear infinite' } : {}} />
                            </button>
                            <button
                                className="uber-btn uber-btn-accent"
                                style={{ width: 'auto', padding: '0.625rem 1.25rem' }}
                                onClick={() => setShowAddModal(true)}
                            >
                                <Plus size={16} /> New Route
                            </button>
                        </div>
                    </div>

                    {/* ======= OVERVIEW TAB ======= */}
                    {activeTab === 'overview' && (
                        <motion.div initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.35 }}>
                            {/* Stats */}
                            <div className="stats-grid">
                                {[
                                    { label: 'Active Fleet', value: routes.length, icon: Bus, color: '#6366f1', trend: '+12%', trendClass: 'trend-up' },
                                    { label: 'Total Personnel', value: employees.length, icon: Users, color: '#8b5cf6', trend: `+${employees.length}`, trendClass: 'trend-up' },
                                    { label: 'Seats Booked', value: totalBooked, icon: Activity, color: '#10b981', trend: `${totalCapacity ? Math.round((totalBooked/totalCapacity)*100) : 0}%`, trendClass: 'trend-stable' },
                                    { label: 'System Uptime', value: '99.9%', icon: ShieldCheck, color: '#0ea5e9', trend: 'Stable', trendClass: 'trend-stable' },
                                ].map((stat, i) => (
                                    <div key={i} className="stat-card">
                                        <div className="flex-between">
                                            <div className="stat-icon" style={{ background: `${stat.color}15`, border: `1px solid ${stat.color}25` }}>
                                                <stat.icon size={22} style={{ color: stat.color }} />
                                            </div>
                                            <span className={`stat-trend ${stat.trendClass}`}>{stat.trend}</span>
                                        </div>
                                        <div>
                                            <p className="stat-label">{stat.label}</p>
                                            <p className="stat-value">{stat.value}</p>
                                        </div>
                                    </div>
                                ))}
                            </div>

                            {/* Charts Row */}
                            <div className="grid-2" style={{ marginTop: '0.5rem' }}>
                                {/* Fleet Occupancy */}
                                <div className="glass-card">
                                    <div className="flex-between" style={{ marginBottom: '1.75rem' }}>
                                        <h3 className="section-title" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                                            <Database size={18} style={{ color: '#6366f1' }} /> Fleet Occupancy
                                        </h3>
                                        <button style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#6366f1', fontSize: '0.7rem', fontWeight: 800, textTransform: 'uppercase', letterSpacing: '0.1em' }}>
                                            Full Report
                                        </button>
                                    </div>
                                    <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
                                        {routes.slice(0, 5).map(r => {
                                            const pct = r.capacity ? Math.round((r.bookedSeats / r.capacity) * 100) : 0;
                                            return (
                                                <div key={r.id}>
                                                    <div className="flex-between" style={{ marginBottom: '0.5rem' }}>
                                                        <div>
                                                            <span style={{ fontWeight: 700, fontSize: '0.875rem' }}>{r.destination}</span>
                                                            <p style={{ fontSize: '0.7rem', color: '#64748b' }}>{r.bookedSeats} of {r.capacity} seats</p>
                                                        </div>
                                                        <span className="font-mono" style={{ fontSize: '0.75rem', fontWeight: 800, color: '#6366f1' }}>{pct}%</span>
                                                    </div>
                                                    <div className="progress-bar">
                                                        <motion.div
                                                            className={`progress-fill ${pct > 80 ? 'progress-fill-danger' : ''}`}
                                                            initial={{ width: 0 }}
                                                            animate={{ width: `${pct}%` }}
                                                            transition={{ duration: 1, ease: 'easeOut' }}
                                                        />
                                                    </div>
                                                </div>
                                            );
                                        })}
                                        {routes.length === 0 && (
                                            <p style={{ textAlign: 'center', color: '#64748b', padding: '2rem 0', fontSize: '0.875rem' }}>No fleet data yet.</p>
                                        )}
                                    </div>
                                </div>

                                 {/* Emergency Monitor */}
                                <div className="glass-card" style={{ border: alerts.some(a => a.type === 'SOS') ? '1px solid rgba(244,63,94,0.4)' : '1px solid var(--border-glass)' }}>
                                    <div className="flex-between" style={{ marginBottom: '1.75rem' }}>
                                        <h3 className="section-title" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                                            <AlertCircle size={18} style={{ color: alerts.some(a => a.type === 'SOS') ? '#f43f5e' : '#8b5cf6' }} /> Emergency Monitor
                                        </h3>
                                        {alerts.length > 0 && <span className="badge badge-error animate-pulse-slow">{alerts.length} Active</span>}
                                    </div>
                                    <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', maxHeight: '400px', overflowY: 'auto', paddingRight: '0.5rem' }}>
                                        {alerts.map((alert) => (
                                            <div key={alert.id} className="glass-card" style={{ padding: '1rem', background: alert.type === 'SOS' ? 'rgba(244,63,94,0.05)' : 'rgba(255,255,255,0.02)' }}>
                                                <div className="flex-between" style={{ marginBottom: '0.5rem' }}>
                                                    <span className={`badge ${alert.type === 'SOS' ? 'badge-error' : 'badge-warning'}`}>{alert.type}</span>
                                                    <span style={{ fontSize: '0.7rem', color: '#64748b' }}>{new Date(alert.timestamp).toLocaleTimeString()}</span>
                                                </div>
                                                <p style={{ fontWeight: 800, fontSize: '0.875rem', marginBottom: '0.25rem' }}>{alert.employee?.name || 'Unknown User'}</p>
                                                <p style={{ fontSize: '0.8rem', color: '#94a3b8', marginBottom: '0.75rem' }}>{alert.message}</p>
                                                <div className="flex-between">
                                                    <span style={{ fontSize: '0.7rem', color: '#6366f1', display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
                                                        <MapPin size={10} /> {alert.location}
                                                    </span>
                                                    <button 
                                                        onClick={() => handleResolveAlert(alert.id)}
                                                        disabled={resolveLoading === alert.id}
                                                        style={{ background: '#10b981', color: 'white', border: 'none', borderRadius: '6px', padding: '0.25rem 0.6rem', fontSize: '0.7rem', fontWeight: 700, cursor: 'pointer' }}
                                                    >
                                                        {resolveLoading === alert.id ? '...' : 'Resolve'}
                                                    </button>
                                                </div>
                                            </div>
                                        ))}
                                        {alerts.length === 0 && (
                                            <div style={{ textAlign: 'center', padding: '3rem 0', opacity: 0.4 }}>
                                                <ShieldCheck size={40} style={{ margin: '0 auto 1rem', color: '#10b981' }} />
                                                <p style={{ fontWeight: 700 }}>System Secure</p>
                                                <p style={{ fontSize: '0.75rem' }}>No active emergencies</p>
                                            </div>
                                        )}
                                    </div>
                            </div>
                            </div>
                        </motion.div>
                    )}

                    {/* ======= FLEET TAB ======= */}
                    {activeTab === 'fleet' && (
                        <motion.div initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.35 }}>
                            <div className="table-card">
                                <div className="table-scroll">
                                    <table>
                                        <thead>
                                            <tr>
                                                <th>Fleet ID</th>
                                                <th>Destination</th>
                                                <th>Departure</th>
                                                <th>Load Factor</th>
                                                <th style={{ textAlign: 'right' }}>Actions</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            {routes.map(r => {
                                                const pct = r.capacity ? Math.round((r.bookedSeats / r.capacity) * 100) : 0;
                                                return (
                                                    <tr key={r.id}>
                                                        <td>
                                                            <span className="font-mono" style={{ fontSize: '0.75rem', background: 'rgba(99,102,241,0.1)', color: '#818cf8', padding: '0.2rem 0.5rem', borderRadius: '6px' }}>
                                                                #FLT-{r.id + 100}
                                                            </span>
                                                        </td>
                                                        <td>
                                                            <p style={{ fontWeight: 700 }}>{r.destination}</p>
                                                            <p style={{ fontSize: '0.7rem', color: '#64748b', marginTop: '0.15rem' }}>From: {r.source || 'Main Hub'}</p>
                                                        </td>
                                                        <td style={{ color: '#94a3b8', fontWeight: 600 }}>{r.pickupTime}</td>
                                                        <td>
                                                            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', minWidth: '120px' }}>
                                                                <div className="progress-bar" style={{ flex: 1 }}>
                                                                    <div
                                                                        className={`progress-fill ${pct > 80 ? 'progress-fill-danger' : ''}`}
                                                                        style={{ width: `${pct}%` }}
                                                                    />
                                                                </div>
                                                                <span className="font-mono" style={{ fontSize: '0.7rem', color: '#94a3b8', flexShrink: 0 }}>{r.bookedSeats}/{r.capacity}</span>
                                                            </div>
                                                        </td>
                                                        <td>
                                                            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'flex-end', gap: '0.5rem' }}>
                                                                <button
                                                                    onClick={() => handleDeleteRoute(r.id)}
                                                                    disabled={deleteLoading === r.id}
                                                                    style={{ background: 'rgba(244,63,94,0.08)', border: '1px solid rgba(244,63,94,0.2)', borderRadius: '8px', padding: '0.4rem', color: '#f43f5e', cursor: 'pointer', display: 'flex', alignItems: 'center' }}
                                                                >
                                                                    {deleteLoading === r.id
                                                                        ? <RefreshCw size={14} style={{ animation: 'spin 1s linear infinite' }} />
                                                                        : <Trash2 size={14} />}
                                                                </button>
                                                            </div>
                                                        </td>
                                                    </tr>
                                                );
                                            })}
                                            {routes.length === 0 && (
                                                <tr>
                                                    <td colSpan="5" style={{ textAlign: 'center', padding: '4rem', opacity: 0.3 }}>
                                                        <Bus size={40} style={{ margin: '0 auto 1rem' }} />
                                                        <p style={{ fontWeight: 700 }}>No fleet deployed</p>
                                                    </td>
                                                </tr>
                                            )}
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                        </motion.div>
                    )}

                    {/* ======= PERSONNEL TAB ======= */}
                    {activeTab === 'personnel' && (
                        <motion.div initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.35 }}>
                            <div className="grid-3">
                                {employees.map((e, i) => (
                                    <motion.div
                                        key={e.id || i}
                                        initial={{ opacity: 0, scale: 0.95 }}
                                        animate={{ opacity: 1, scale: 1 }}
                                        transition={{ delay: i * 0.05 }}
                                        className="glass-card"
                                        style={{ display: 'flex', alignItems: 'center', gap: '1.125rem' }}
                                    >
                                        <div className="avatar" style={{ width: 56, height: 56, borderRadius: 16, background: 'rgba(99,102,241,0.1)', border: '1px solid rgba(99,102,241,0.2)', color: '#6366f1', fontSize: '1.25rem' }}>
                                            {e.name?.[0]?.toUpperCase() || '?'}
                                        </div>
                                        <div style={{ flex: 1, minWidth: 0 }}>
                                            <h4 style={{ fontWeight: 800, marginBottom: '0.2rem', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{e.name}</h4>
                                            <p className="font-mono" style={{ fontSize: '0.7rem', color: '#64748b', marginBottom: '0.5rem', overflow: 'hidden', textOverflow: 'ellipsis' }}>{e.email}</p>
                                            <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
                                                <span className="badge badge-info">{e.role}</span>
                                                <span style={{ fontSize: '0.65rem', color: '#64748b' }}>Active</span>
                                            </div>
                                        </div>
                                    </motion.div>
                                ))}
                                {employees.length === 0 && (
                                    <div className="glass-card" style={{ gridColumn: '1/-1', textAlign: 'center', padding: '4rem', opacity: 0.3 }}>
                                        <Users size={40} style={{ margin: '0 auto 1rem' }} />
                                        <p style={{ fontWeight: 700 }}>No employees found</p>
                                    </div>
                                )}
                            </div>
                        </motion.div>
                    )}

                    {/* ======= SETTINGS TAB ======= */}
                    {activeTab === 'config' && (
                        <motion.div initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.35 }} style={{ maxWidth: '640px' }}>
                            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', marginBottom: '2rem' }}>
                                {[
                                    { label: 'Booking Window', desc: 'Hours before departure to close bookings', value: '2' },
                                    { label: 'Auto-Refresh Rate', desc: 'Seconds between fleet data updates', value: '30' },
                                    { label: 'System Notifications', desc: 'Enable global SMS alerts for delays', toggle: true },
                                    { label: 'Max Bookings Per User', desc: 'Limit simultaneous active bookings', value: '1' },
                                ].map((item, i) => (
                                    <div key={i} className="glass-card flex-between">
                                        <div>
                                            <p style={{ fontWeight: 700, marginBottom: '0.2rem' }}>{item.label}</p>
                                            <p style={{ fontSize: '0.78rem', color: '#64748b' }}>{item.desc}</p>
                                        </div>
                                        {item.toggle ? (
                                            <div className="toggle"><div className="toggle-thumb" /></div>
                                        ) : (
                                            <input
                                                className="uber-input-plain"
                                                style={{ width: '80px', textAlign: 'center', padding: '0.5rem', fontSize: '0.875rem' }}
                                                defaultValue={item.value}
                                            />
                                        )}
                                    </div>
                                ))}
                            </div>
                            <button className="uber-btn uber-btn-accent" style={{ width: 'auto', padding: '0.75rem 2rem' }}>
                                Save Configuration
                            </button>
                        </motion.div>
                    )}
                </div>
            </main>

            {/* Add Route Modal */}
            <AnimatePresence>
                {showAddModal && (
                    <motion.div
                        className="modal-overlay"
                        initial={{ opacity: 0 }}
                        animate={{ opacity: 1 }}
                        exit={{ opacity: 0 }}
                        onClick={(e) => e.target === e.currentTarget && setShowAddModal(false)}
                    >
                        <motion.div
                            className="modal-card"
                            initial={{ opacity: 0, scale: 0.94, y: 20 }}
                            animate={{ opacity: 1, scale: 1, y: 0 }}
                            exit={{ opacity: 0, scale: 0.94, y: 20 }}
                        >
                            <div className="flex-between" style={{ marginBottom: '1.75rem' }}>
                                <h2 style={{ fontWeight: 900, fontSize: '1.375rem', letterSpacing: '-0.02em' }}>Deploy New Route</h2>
                                <button
                                    onClick={() => setShowAddModal(false)}
                                    style={{ background: 'rgba(255,255,255,0.06)', border: 'none', borderRadius: '10px', padding: '0.4rem', cursor: 'pointer', color: '#94a3b8' }}
                                >
                                    <X size={18} />
                                </button>
                            </div>
                            <form onSubmit={handleAddRoute} style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
                                <div className="grid-2">
                                    <div className="input-group">
                                        <label className="input-label">Origin</label>
                                        <input className="uber-input-plain" placeholder="Terminal A" value={newRoute.source} onChange={e => setNewRoute({ ...newRoute, source: e.target.value })} required />
                                    </div>
                                    <div className="input-group">
                                        <label className="input-label">Destination</label>
                                        <input className="uber-input-plain" placeholder="HQ Building" value={newRoute.destination} onChange={e => setNewRoute({ ...newRoute, destination: e.target.value })} required />
                                    </div>
                                </div>
                                <div className="grid-2">
                                    <div className="input-group">
                                        <label className="input-label">Departure Time</label>
                                        <input className="uber-input-plain" placeholder="08:00 AM" value={newRoute.pickupTime} onChange={e => setNewRoute({ ...newRoute, pickupTime: e.target.value })} required />
                                    </div>
                                    <div className="input-group">
                                        <label className="input-label">Seat Capacity</label>
                                        <input type="number" className="uber-input-plain" value={newRoute.capacity} min={1} onChange={e => setNewRoute({ ...newRoute, capacity: parseInt(e.target.value) })} required />
                                    </div>
                                </div>
                                <button type="submit" className="uber-btn uber-btn-accent" style={{ marginTop: '0.5rem' }}>
                                    Confirm Deployment
                                </button>
                            </form>
                        </motion.div>
                    </motion.div>
                )}
            </AnimatePresence>
        </div>
    );
};

export default AdminDashboard;
