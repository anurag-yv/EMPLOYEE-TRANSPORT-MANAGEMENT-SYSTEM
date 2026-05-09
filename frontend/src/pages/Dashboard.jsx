import React, { useEffect, useState, useRef, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api';
import { Bus, LogOut, Search, Clock, MapPin, Navigation, History, Shield, Menu, ArrowRight, RefreshCw, Star, CheckCircle2, ShieldCheck, Zap, ChevronRight, AlertTriangle, Phone, X, XCircle } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import { parseJwt } from '../utils/jwt';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer } from 'recharts';

const POLL_INTERVAL = 15000;

export default function Dashboard() {
  const [routes, setRoutes] = useState([]);
  const [myBookings, setMyBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [bookingLoading, setBookingLoading] = useState(null);
  const [cancelLoading, setCancelLoading] = useState(null);
  const [search, setSearch] = useState('');
  const [analyticsData, setAnalyticsData] = useState(null);
  const [userName, setUserName] = useState('Employee');
  const [tab, setTab] = useState('commute');
  const [menuOpen, setMenuOpen] = useState(false);
  const [toast, setToast] = useState(null);
  const [sosActive, setSosActive] = useState(false);
  const [locationShared, setLocationShared] = useState(false);
  const [currentTime, setCurrentTime] = useState(new Date());
  const pollRef = useRef(null);
  const navigate = useNavigate();

  const showToast = (msg, type = 'success') => {
    setToast({ msg, type });
    setTimeout(() => setToast(null), 3500);
  };

  const fetchData = useCallback(async () => {
    try {
      const [r, b, a] = await Promise.all([
        api.get('/api/routes'), 
        api.get('/api/booking/my'),
        api.get('/api/analytics/dashboard').catch(() => ({ data: null }))
      ]);
      setRoutes(Array.isArray(r.data) ? r.data : []);
      setMyBookings(Array.isArray(b.data) ? b.data : []);
      if (a.data) setAnalyticsData(a.data);
    } catch (e) { console.error(e); }
    finally { setLoading(false); }
  }, []);

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) { navigate('/login'); return; }
    try { const u = parseJwt(token); if (u?.sub) setUserName(u.sub.split('@')[0]); } catch (e) {}
    fetchData();
    pollRef.current = setInterval(fetchData, POLL_INTERVAL);
    const clock = setInterval(() => setCurrentTime(new Date()), 1000);
    return () => { clearInterval(pollRef.current); clearInterval(clock); };
  }, [navigate, fetchData]);

  const handleBook = async (routeId) => {
    setBookingLoading(routeId);
    try { await api.post('/api/booking', { routeId }); await fetchData(); showToast('Ride booked! 🎉'); }
    catch (e) { showToast(e.response?.data?.message || 'Booking failed.', 'error'); }
    finally { setBookingLoading(null); }
  };

  const handleCancel = async (bookingId) => {
    if (!window.confirm('Cancel this booking?')) return;
    setCancelLoading(bookingId);
    try { await api.delete(`/api/booking/${bookingId}`); await fetchData(); showToast('Booking cancelled.'); }
    catch (e) { showToast('Cancel failed.', 'error'); }
    finally { setCancelLoading(null); }
  };

  const handleSOS = async () => {
    setSosActive(true);
    try {
      await api.post('/api/alerts', { type: 'SOS', message: 'Emergency SOS triggered by employee', location: 'Active Trip' });
      showToast('🚨 SOS Alert sent to security team!', 'error');
    } catch (e) {
      showToast('Failed to send SOS. Please call security.', 'error');
    }
    setTimeout(() => setSosActive(false), 5000);
  };

  const handleShareLocation = () => {
    if (!navigator.geolocation) { showToast('Geolocation not supported.', 'error'); return; }
    navigator.geolocation.getCurrentPosition(
      async (pos) => { 
        const locStr = `${pos.coords.latitude.toFixed(4)}, ${pos.coords.longitude.toFixed(4)}`;
        try {
          await api.post('/api/alerts', { type: 'LOCATION_SHARE', message: 'Location sharing started', location: locStr });
          setLocationShared(true); 
          showToast(`📍 Location shared: ${locStr}`); 
        } catch (e) {
          showToast('Failed to share location.', 'error');
        }
      },
      () => { showToast('Location access denied.', 'error'); }
    );
  };

  const navItems = [
    { id: 'commute', label: 'Commute', icon: MapPin },
    { id: 'history', label: 'My Rides', icon: History },
    { id: 'safety', label: 'Safety Hub', icon: Shield },
    { id: 'analytics', label: 'Analytics', icon: Zap },
  ];

  const filtered = routes.filter(r => r.destination?.toLowerCase().includes(search.toLowerCase()) || r.source?.toLowerCase().includes(search.toLowerCase()));
  const activeBooking = myBookings.find(b => b.status === 'CONFIRMED' || !b.status);

  const SidebarContent = ({ close }) => (
    <div className="sidebar-inner">
      <div className="sidebar-logo">
        <div className="sidebar-logo-icon white-bg"><Navigation size={20} /></div>
        <span className="sidebar-logo-text">TransitPort</span>
      </div>
      <nav className="sidebar-nav">
        {navItems.map(n => (
          <button key={n.id} onClick={() => { setTab(n.id); close && close(); }} className={`nav-btn ${tab === n.id ? 'active' : ''}`}>
            <n.icon size={17} /><span>{n.label}</span>
          </button>
        ))}
      </nav>
      <div className="sidebar-footer">
        <button onClick={() => { localStorage.removeItem('token'); navigate('/login'); }} className="logout-btn">
          <LogOut size={17} /><span>Logout</span>
        </button>
      </div>
    </div>
  );

  return (
    <div className="dashboard-layout">
      <div className="uber-bg" />

      <AnimatePresence>
        {toast && (
          <motion.div initial={{ opacity: 0, y: -20 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0, y: -20 }}
            style={{ position:'fixed', top:'1rem', right:'1rem', zIndex:999, padding:'0.875rem 1.25rem', borderRadius:'12px', maxWidth:'320px',
              background: toast.type==='error' ? 'rgba(244,63,94,0.15)' : 'rgba(16,185,129,0.15)',
              border:`1px solid ${toast.type==='error' ? 'rgba(244,63,94,0.4)' : 'rgba(16,185,129,0.4)'}`,
              color: toast.type==='error' ? '#f43f5e' : '#10b981', fontWeight:700, fontSize:'0.875rem' }}>
            {toast.msg}
          </motion.div>
        )}
      </AnimatePresence>

      <aside className="sidebar"><SidebarContent /></aside>

      <header className="mobile-header">
        <div className="mobile-header-logo"><Navigation size={18} /> TransitPort</div>
        <button className="menu-toggle-btn" onClick={() => setMenuOpen(true)}><Menu size={20} /></button>
      </header>

      <div className={`mobile-overlay ${menuOpen ? 'open' : ''}`}>
        <div className="mobile-overlay-bg" onClick={() => setMenuOpen(false)} />
        <div className="mobile-sidebar"><SidebarContent close={() => setMenuOpen(false)} /></div>
      </div>

      <main className="main-content">
        <div className="page-container">
          <div className="page-header">
            <div>
              <h1 className="page-title">Hi, {userName} 👋</h1>
              <p className="page-subtitle">{currentTime.toLocaleTimeString()} · {currentTime.toLocaleDateString('en-US', { weekday:'long', month:'short', day:'numeric' })}</p>
            </div>
            <div className="header-actions">
              <button className="btn-icon" onClick={() => { setLoading(true); fetchData(); }} title="Refresh">
                <RefreshCw size={17} style={loading ? { animation:'spin 1s linear infinite' } : {}} />
              </button>
              <div style={{ width:40,height:40,borderRadius:12,background:'linear-gradient(135deg,#6366f1,#8b5cf6)',display:'flex',alignItems:'center',justifyContent:'center',fontWeight:800,color:'white' }}>
                {userName[0]?.toUpperCase()}
              </div>
            </div>
          </div>

          {/* COMMUTE TAB */}
          {tab === 'commute' && (
            <motion.div initial={{ opacity:0, y:16 }} animate={{ opacity:1, y:0 }}>
              {/* Active Booking Banner */}
              {activeBooking && (
                <motion.div initial={{ opacity: 0, scale: 0.98 }} animate={{ opacity: 1, scale: 1 }} className="glass-card" style={{ border: '1px solid var(--accent)', marginBottom: '1.5rem', padding: '1.5rem', overflow:'hidden', position:'relative' }}>
                  <div style={{ display:'flex', justifyContent:'space-between', alignItems:'flex-start', marginBottom:'1.5rem', flexWrap:'wrap', gap:'1rem' }}>
                    <div>
                      <div style={{ display:'flex', alignItems:'center', gap:'0.5rem', marginBottom:'0.5rem' }}>
                        <span className="badge badge-success animate-pulse-slow" style={{ fontSize:'0.65rem' }}>● Live Trip</span>
                        <span style={{ fontSize:'0.7rem', color:'#64748b', fontWeight:700 }}>Vehicle #TZ-{activeBooking.id + 500}</span>
                      </div>
                      <h2 style={{ fontWeight: 900, fontSize: '1.25rem' }}>{activeBooking.route?.destination}</h2>
                      <p style={{ color: 'var(--text-secondary)', fontSize: '0.8rem' }}>Boarding at {activeBooking.route?.pickupTime} from {activeBooking.route?.source || 'Main Hub'}</p>
                    </div>
                    <button 
                      onClick={() => handleCancel(activeBooking.id)}
                      disabled={cancelLoading === activeBooking.id}
                      style={{ background:'rgba(244,63,94,0.1)', border:'1px solid rgba(244,63,94,0.2)', borderRadius:10, padding:'0.5rem 1rem', color:'#f43f5e', cursor:'pointer', fontWeight:700, fontSize:'0.75rem', display:'flex', alignItems:'center', gap:'0.4rem' }}
                    >
                      {cancelLoading === activeBooking.id ? <RefreshCw size={14} style={{ animation:'spin 1s linear infinite' }} /> : <><X size={14} /> Cancel Ride</>}
                    </button>
                  </div>

                  {/* Trip Tracker Visualization */}
                  <div style={{ background: 'rgba(0,0,0,0.25)', borderRadius: '16px', padding: '1.25rem', border: '1px solid var(--border-glass)' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '1.25rem' }}>
                      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '4px' }}>
                        <div style={{ width: '10px', height: '10px', borderRadius: '50%', background: 'var(--accent)', boxShadow: '0 0 10px var(--accent)' }} />
                        <div style={{ width: '2px', height: '35px', background: 'linear-gradient(to bottom, var(--accent), transparent)' }} />
                      </div>
                      <div style={{ flex: 1 }}>
                        <p style={{ fontSize: '0.65rem', fontWeight: 800, color: 'var(--accent)', textTransform: 'uppercase', letterSpacing:'0.05em' }}>Pickup Point</p>
                        <p style={{ fontWeight: 700, fontSize: '0.9rem' }}>{activeBooking.route?.source || 'Main Hub'}</p>
                      </div>
                      <div style={{ textAlign:'right' }}>
                        <p style={{ fontSize:'0.85rem', fontWeight:900, color:'white' }}>08:00 AM</p>
                        <p style={{ fontSize:'0.65rem', color:'#64748b' }}>Scheduled</p>
                      </div>
                    </div>
                    
                    <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                        <div style={{ width: '10px', height: '10px', borderRadius: '2px', background: '#94a3b8', opacity: 0.4 }} />
                      </div>
                      <div style={{ flex: 1 }}>
                        <p style={{ fontSize: '0.65rem', fontWeight: 800, color: '#64748b', textTransform: 'uppercase', letterSpacing:'0.05em' }}>Destination</p>
                        <p style={{ fontWeight: 700, fontSize: '0.9rem', opacity: 0.7 }}>{activeBooking.route?.destination}</p>
                      </div>
                    </div>

                    <div style={{ marginTop: '1.5rem', paddingTop: '1rem', borderTop: '1px dashed var(--border-glass)' }}>
                      <div className="flex-between" style={{ marginBottom: '0.65rem', fontSize: '0.75rem', fontWeight: 700 }}>
                        <span style={{ color: 'var(--accent)', display:'flex', alignItems:'center', gap:'0.4rem' }}>
                          <Zap size={12} fill="var(--accent)" /> Vehicle arriving in 8 mins
                        </span>
                        <span style={{ color: '#64748b' }}>2.4 km away</span>
                      </div>
                      <div className="progress-bar" style={{ height:'6px' }}>
                        <motion.div 
                          className="progress-fill" 
                          initial={{ width: '15%' }}
                          animate={{ width: '35%' }}
                          transition={{ duration: 3, repeat: Infinity, repeatType: 'reverse', ease: 'easeInOut' }}
                        />
                      </div>
                    </div>
                  </div>
                </motion.div>
              )}

              {/* Search */}
              <div style={{ position:'relative', marginBottom:'1.5rem' }}>
                <Search style={{ position:'absolute', left:'1rem', top:'50%', transform:'translateY(-50%)', color:'#64748b' }} size={16} />
                <input className="uber-input" style={{ paddingLeft:'2.75rem' }} placeholder="Search routes by destination or origin..." value={search} onChange={e => setSearch(e.target.value)} />
              </div>

              {/* Routes Grid */}
              <div style={{ display:'grid', gridTemplateColumns:'repeat(auto-fill, minmax(300px, 1fr))', gap:'1rem' }}>
                <AnimatePresence mode="popLayout">
                  {filtered.map((route, i) => {
                    const pct = route.capacity ? Math.round((route.bookedSeats / route.capacity) * 100) : 0;
                    const seatsLeft = route.capacity - route.bookedSeats;
                    const isFull = seatsLeft <= 0;
                    const isLow = seatsLeft <= 5 && !isFull;
                    const alreadyBooked = myBookings.some(b => b.route?.id === route.id);
                    return (
                      <motion.div key={route.id} initial={{ opacity:0, scale:0.96 }} animate={{ opacity:1, scale:1 }} exit={{ opacity:0 }} transition={{ delay:i*0.04 }}
                        className="glass-card" style={{ padding:'1.5rem', cursor:'default', display:'flex', flexDirection:'column', gap:'1rem' }}>
                        <div style={{ display:'flex', alignItems:'flex-start', justifyContent:'space-between', gap:'0.75rem' }}>
                          <div style={{ display:'flex', alignItems:'center', gap:'0.875rem', flex:1, minWidth:0 }}>
                            <div style={{ width:44,height:44,borderRadius:12,background:'rgba(99,102,241,0.12)',border:'1px solid rgba(99,102,241,0.2)',display:'flex',alignItems:'center',justifyContent:'center',flexShrink:0 }}>
                              <Bus size={20} style={{ color:'#6366f1' }} />
                            </div>
                            <div style={{ minWidth:0 }}>
                              <p style={{ fontWeight:900, fontSize:'1rem', marginBottom:'0.2rem', overflow:'hidden', textOverflow:'ellipsis', whiteSpace:'nowrap' }}>{route.destination}</p>
                              <p style={{ fontSize:'0.75rem', color:'#64748b', display:'flex', alignItems:'center', gap:'0.35rem' }}>
                                <MapPin size={11} /> {route.source || 'Main Hub'} → {route.destination}
                              </p>
                            </div>
                          </div>
                          {alreadyBooked && <span className="badge badge-success">Booked</span>}
                          {isFull && !alreadyBooked && <span className="badge badge-error">Full</span>}
                          {isLow && !alreadyBooked && <span className="badge badge-warning">{seatsLeft} left</span>}
                        </div>

                        <div style={{ display:'flex', alignItems:'center', gap:'1.25rem', fontSize:'0.8rem', color:'#94a3b8' }}>
                          <span style={{ display:'flex', alignItems:'center', gap:'0.35rem' }}><Clock size={13} style={{ color:'#6366f1' }} />{route.pickupTime}</span>
                          <span style={{ display:'flex', alignItems:'center', gap:'0.35rem' }}><Star size={13} style={{ color:'#f59e0b' }} />{seatsLeft} seats available</span>
                        </div>

                        <div>
                          <div style={{ display:'flex', justifyContent:'space-between', fontSize:'0.7rem', marginBottom:'0.35rem' }}>
                            <span style={{ color:'#64748b' }}>Occupancy</span>
                            <span style={{ color: pct > 80 ? '#f43f5e' : '#6366f1', fontWeight:700 }}>{pct}%</span>
                          </div>
                          <div className="progress-bar">
                            <div className={`progress-fill ${pct > 80 ? 'progress-fill-danger' : ''}`} style={{ width:`${pct}%`, transition:'width 0.8s ease' }} />
                          </div>
                        </div>

                        <button onClick={() => !alreadyBooked && !isFull && handleBook(route.id)}
                          disabled={isFull || alreadyBooked || bookingLoading === route.id}
                          style={{ width:'100%', padding:'0.75rem', borderRadius:12, border:'none', cursor: isFull||alreadyBooked ? 'not-allowed':'pointer',
                            background: alreadyBooked ? 'rgba(16,185,129,0.1)' : isFull ? 'rgba(255,255,255,0.04)' : 'white',
                            color: alreadyBooked ? '#10b981' : isFull ? '#64748b' : 'black',
                            fontWeight:700, fontSize:'0.875rem', display:'flex', alignItems:'center', justifyContent:'center', gap:'0.5rem',
                            transition:'all 0.2s', border: alreadyBooked ? '1px solid rgba(16,185,129,0.3)' : 'none' }}>
                          {bookingLoading === route.id ? <RefreshCw size={16} style={{ animation:'spin 1s linear infinite' }} /> :
                            alreadyBooked ? <><CheckCircle2 size={16} />Already Booked</> :
                            isFull ? 'Fully Booked' : <><ArrowRight size={16} />Book This Ride</>}
                        </button>
                      </motion.div>
                    );
                  })}
                </AnimatePresence>
              </div>
              {filtered.length === 0 && !loading && (
                <div className="glass-card" style={{ textAlign:'center', padding:'4rem', opacity:0.4 }}>
                  <Zap size={40} style={{ margin:'0 auto 1rem' }} />
                  <p style={{ fontWeight:700 }}>{search ? 'No routes match your search' : 'No routes available'}</p>
                </div>
              )}
            </motion.div>
          )}

          {/* HISTORY TAB */}
          {tab === 'history' && (
            <motion.div initial={{ opacity:0, y:16 }} animate={{ opacity:1, y:0 }}>
              <div style={{ marginBottom:'1.75rem' }}>
                <h3 style={{ fontSize:'1.75rem', fontWeight:900, letterSpacing:'-0.03em' }}>My Rides</h3>
                <p className="section-sub">{myBookings.length} total booking{myBookings.length !== 1 ? 's' : ''}</p>
              </div>
              <div style={{ display:'flex', flexDirection:'column', gap:'1rem' }}>
                {myBookings.map(b => (
                  <div key={b.id} className="glass-card" style={{ display:'flex', alignItems:'center', gap:'1.25rem', padding:'1.25rem 1.5rem', flexWrap:'wrap' }}>
                    <div style={{ width:48,height:48,borderRadius:14,background:'rgba(99,102,241,0.1)',border:'1px solid rgba(99,102,241,0.2)',display:'flex',alignItems:'center',justifyContent:'center',flexShrink:0 }}>
                      <Bus size={20} style={{ color:'#6366f1' }} />
                    </div>
                    <div style={{ flex:1, minWidth:'180px' }}>
                      <p style={{ fontWeight:800, fontSize:'1rem', marginBottom:'0.25rem' }}>{b.route?.destination}</p>
                      <p style={{ fontSize:'0.75rem', color:'#64748b', display:'flex', alignItems:'center', gap:'0.5rem', flexWrap:'wrap' }}>
                        <span style={{ display:'flex', alignItems:'center', gap:'0.3rem' }}><MapPin size={11} />{b.route?.source || 'Main Hub'}</span>
                        <span>·</span>
                        <span style={{ display:'flex', alignItems:'center', gap:'0.3rem' }}><Clock size={11} />{b.route?.pickupTime}</span>
                      </p>
                    </div>
                    <div style={{ display:'flex', alignItems:'center', gap:'0.875rem' }}>
                      <span className={`badge ${b.status === 'CONFIRMED' || !b.status ? 'badge-success' : 'badge-error'}`}>
                        {b.status || 'Confirmed'}
                      </span>
                      <span className="font-mono" style={{ fontSize:'0.7rem', color:'#6366f1', background:'rgba(99,102,241,0.1)', padding:'0.2rem 0.5rem', borderRadius:6 }}>
                        #TP-{(b.id + 1000)}
                      </span>
                      <button onClick={() => handleCancel(b.id)} disabled={cancelLoading === b.id}
                        style={{ background:'rgba(244,63,94,0.08)',border:'1px solid rgba(244,63,94,0.2)',borderRadius:8,padding:'0.4rem 0.75rem',color:'#f43f5e',cursor:'pointer',fontWeight:700,fontSize:'0.75rem',display:'flex',alignItems:'center',gap:'0.3rem' }}>
                        {cancelLoading === b.id ? <RefreshCw size={12} style={{ animation:'spin 1s linear infinite' }} /> : <><X size={12} />Cancel</>}
                      </button>
                    </div>
                  </div>
                ))}
                {myBookings.length === 0 && (
                  <div className="glass-card" style={{ textAlign:'center', padding:'5rem', opacity:0.3 }}>
                    <History size={48} style={{ margin:'0 auto 1rem' }} />
                    <p style={{ fontWeight:700, fontSize:'1.125rem' }}>No bookings yet</p>
                    <p style={{ color:'#64748b', marginTop:'0.5rem', fontSize:'0.875rem' }}>Book a ride from the Commute tab</p>
                  </div>
                )}
              </div>
            </motion.div>
          )}

          {/* SAFETY TAB */}
          {tab === 'safety' && (
            <motion.div initial={{ opacity:0, y:16 }} animate={{ opacity:1, y:0 }}>
              <div style={{ marginBottom:'1.75rem' }}>
                <h3 style={{ fontSize:'1.75rem', fontWeight:900, letterSpacing:'-0.03em' }}>Safety Hub</h3>
                <p className="section-sub">Your security controls — always within reach</p>
              </div>

              {/* SOS Button */}
              <div style={{ background: sosActive ? 'rgba(244,63,94,0.15)' : 'rgba(244,63,94,0.05)', border:`1px solid ${sosActive ? 'rgba(244,63,94,0.5)' : 'rgba(244,63,94,0.2)'}`, borderRadius:20, padding:'2rem', marginBottom:'1.5rem', textAlign:'center', transition:'all 0.3s' }}>
                <motion.button onClick={handleSOS} whileTap={{ scale:0.95 }} animate={sosActive ? { scale:[1,1.05,1] } : {}} transition={{ repeat: sosActive ? Infinity : 0, duration:0.8 }}
                  style={{ width:100,height:100,borderRadius:'50%',background: sosActive ? '#f43f5e' : 'rgba(244,63,94,0.15)',border:'3px solid rgba(244,63,94,0.5)',cursor:'pointer',display:'flex',flexDirection:'column',alignItems:'center',justifyContent:'center',gap:'0.25rem',margin:'0 auto 1.25rem',boxShadow: sosActive ? '0 0 40px rgba(244,63,94,0.5)' : 'none',transition:'all 0.3s' }}>
                  <AlertTriangle size={32} style={{ color: sosActive ? 'white' : '#f43f5e' }} />
                  <span style={{ fontSize:'0.6rem',fontWeight:900,color: sosActive ? 'white' : '#f43f5e',letterSpacing:'0.1em' }}>SOS</span>
                </motion.button>
                <h4 style={{ fontWeight:800, fontSize:'1.125rem', marginBottom:'0.5rem', color: sosActive ? '#f43f5e' : 'white' }}>
                  {sosActive ? '🚨 Alert Sent! Help is on the way' : 'Emergency SOS'}
                </h4>
                <p style={{ fontSize:'0.825rem', color:'#94a3b8' }}>Press to instantly alert the security team and emergency contacts</p>
              </div>

              {/* Location + Contact */}
              <div className="grid-2" style={{ marginBottom:'1.5rem' }}>
                <div className="glass-card" style={{ textAlign:'center' }}>
                  <div style={{ width:56,height:56,borderRadius:16,background:'rgba(99,102,241,0.12)',border:'1px solid rgba(99,102,241,0.2)',display:'flex',alignItems:'center',justifyContent:'center',margin:'0 auto 1rem' }}>
                    <MapPin size={24} style={{ color:'#6366f1' }} />
                  </div>
                  <h4 style={{ fontWeight:800, marginBottom:'0.5rem' }}>Share Live Location</h4>
                  <p style={{ fontSize:'0.8rem', color:'#64748b', marginBottom:'1.25rem' }}>Share your real-time GPS coordinates with family or security</p>
                  <button onClick={handleShareLocation}
                    style={{ width:'100%', padding:'0.75rem', borderRadius:12, border:'none', cursor:'pointer', fontWeight:700, fontSize:'0.875rem',
                      background: locationShared ? 'rgba(16,185,129,0.15)' : '#6366f1',
                      color: locationShared ? '#10b981' : 'white', display:'flex', alignItems:'center', justifyContent:'center', gap:'0.5rem' }}>
                    {locationShared ? <><CheckCircle2 size={16} />Location Shared</> : <><MapPin size={16} />Share Location</>}
                  </button>
                </div>

                <div className="glass-card" style={{ textAlign:'center' }}>
                  <div style={{ width:56,height:56,borderRadius:16,background:'rgba(16,185,129,0.12)',border:'1px solid rgba(16,185,129,0.2)',display:'flex',alignItems:'center',justifyContent:'center',margin:'0 auto 1rem' }}>
                    <Phone size={24} style={{ color:'#10b981' }} />
                  </div>
                  <h4 style={{ fontWeight:800, marginBottom:'0.5rem' }}>Emergency Contacts</h4>
                  <p style={{ fontSize:'0.8rem', color:'#64748b', marginBottom:'1.25rem' }}>Security Control Room available 24/7</p>
                  <a href="tel:+911" style={{ display:'block', width:'100%', padding:'0.75rem', borderRadius:12, background:'rgba(16,185,129,0.15)', border:'1px solid rgba(16,185,129,0.3)', color:'#10b981', fontWeight:700, fontSize:'0.875rem', textDecoration:'none', textAlign:'center' }}>
                    📞 Call Security: 911
                  </a>
                </div>
              </div>

              {/* Safety Features */}
              <div className="grid-3" style={{ marginBottom:'1.5rem' }}>
                {[
                  { title:'GPS Tracking', desc:'Every bus has live GPS — your family can track your ride', icon:Navigation, color:'#0ea5e9', active:true },
                  { title:'Verified Drivers', desc:'All drivers are background-checked and certified', icon:ShieldCheck, color:'#10b981', active:true },
                  { title:'24/7 Monitoring', desc:'Control room monitors all active routes in real-time', icon:Shield, color:'#8b5cf6', active:true },
                ].map((item, i) => (
                  <div key={i} className="glass-card" style={{ display:'flex', flexDirection:'column', gap:'1rem' }}>
                    <div style={{ display:'flex', alignItems:'center', justifyContent:'space-between' }}>
                      <div style={{ width:44,height:44,borderRadius:12,background:`${item.color}15`,border:`1px solid ${item.color}25`,display:'flex',alignItems:'center',justifyContent:'center' }}>
                        <item.icon size={20} style={{ color:item.color }} />
                      </div>
                      {item.active && <span className="badge badge-success">Active</span>}
                    </div>
                    <div>
                      <h4 style={{ fontWeight:800, marginBottom:'0.35rem' }}>{item.title}</h4>
                      <p style={{ fontSize:'0.8rem', color:'#64748b', lineHeight:1.5 }}>{item.desc}</p>
                    </div>
                  </div>
                ))}
              </div>

              {/* Safety Banner */}
              <div className="glass-card" style={{ background:'rgba(16,185,129,0.04)', borderColor:'rgba(16,185,129,0.2)', display:'flex', alignItems:'center', gap:'1.5rem', flexWrap:'wrap' }}>
                <div style={{ width:64,height:64,borderRadius:'50%',background:'rgba(16,185,129,0.15)',display:'flex',alignItems:'center',justifyContent:'center',flexShrink:0 }}>
                  <ShieldCheck size={32} style={{ color:'#10b981' }} />
                </div>
                <div style={{ flex:1, minWidth:'200px' }}>
                  <h4 style={{ fontWeight:800, marginBottom:'0.4rem' }}>TransitPort Safety Standard</h4>
                  <p style={{ fontSize:'0.825rem', color:'#94a3b8', lineHeight:1.6 }}>Every ride is GPS-tracked. Emergency response team available 24/7. All buses undergo monthly safety inspections. Incident reporting available at any time.</p>
                </div>
              </div>
            </motion.div>
          )}

          {/* ANALYTICS TAB */}
          {tab === 'analytics' && (
            <motion.div initial={{ opacity:0, y:16 }} animate={{ opacity:1, y:0 }}>
              <div style={{ marginBottom:'1.75rem' }}>
                <h3 style={{ fontSize:'1.75rem', fontWeight:900, letterSpacing:'-0.03em' }}>Analytics Dashboard</h3>
                <p className="section-sub">API Base URL: {import.meta.env.VITE_API_BASE_URL || window.location.origin}</p>
              </div>

              {!analyticsData ? (
                 <div className="glass-card" style={{ padding: '2rem', textAlign: 'center' }}>
                    <p>No analytics data available or unauthorized access.</p>
                 </div>
              ) : (
                <>
                  <div className="grid-3" style={{ marginBottom: '1.5rem' }}>
                    <div className="glass-card">
                      <h4 style={{ color: '#64748b', fontSize: '0.875rem' }}>Total Bookings</h4>
                      <p style={{ fontSize: '2rem', fontWeight: 900, color: '#6366f1' }}>{analyticsData.totalBookings}</p>
                    </div>
                    <div className="glass-card">
                      <h4 style={{ color: '#64748b', fontSize: '0.875rem' }}>Active Routes</h4>
                      <p style={{ fontSize: '2rem', fontWeight: 900, color: '#10b981' }}>{analyticsData.activeRoutes}</p>
                    </div>
                    <div className="glass-card">
                      <h4 style={{ color: '#64748b', fontSize: '0.875rem' }}>Total Employees</h4>
                      <p style={{ fontSize: '2rem', fontWeight: 900, color: '#f59e0b' }}>{analyticsData.totalEmployees}</p>
                    </div>
                  </div>

                  <div className="glass-card" style={{ padding: '2rem', height: '300px' }}>
                    <h4 style={{ fontWeight: 800, marginBottom: '1.5rem' }}>Weekly Usage</h4>
                    <ResponsiveContainer width="100%" height="100%">
                      <BarChart data={Object.entries(analyticsData.weeklyUsage || {}).map(([name, uv]) => ({ name, uv }))}>
                        <XAxis dataKey="name" stroke="#94a3b8" />
                        <YAxis stroke="#94a3b8" />
                        <Tooltip cursor={{fill: 'rgba(255,255,255,0.05)'}} contentStyle={{backgroundColor: '#1e293b', border: 'none', borderRadius: '8px'}} />
                        <Bar dataKey="uv" fill="#6366f1" radius={[4, 4, 0, 0]} />
                      </BarChart>
                    </ResponsiveContainer>
                  </div>
                </>
              )}
            </motion.div>
          )}
        </div>
      </main>
    </div>
  );
}
