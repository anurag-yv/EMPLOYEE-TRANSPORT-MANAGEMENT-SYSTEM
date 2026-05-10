import React, { useEffect, useState, useRef, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api';
import { Bus, LogOut, Search, Clock, MapPin, Navigation, History, Shield, Menu, ArrowRight, RefreshCw, Star, CheckCircle2, ShieldCheck, Zap, ChevronRight, AlertTriangle, Phone, X, XCircle, MessageSquare, Send } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import { parseJwt } from '../utils/jwt';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer } from 'recharts';

const POLL_INTERVAL = 15000;

export default function Dashboard() {
  const [routes, setRoutes] = useState([]);
  const [myBookings, setMyBookings] = useState([]);
  const [myAlerts, setMyAlerts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [bookingLoading, setBookingLoading] = useState(null);
  const [cancelLoading, setCancelLoading] = useState(null);
  const [search, setSearch] = useState('');
  const [mapSearch, setMapSearch] = useState('');
  const [analyticsData, setAnalyticsData] = useState(null);
  const [userName, setUserName] = useState('User');
  const [userRole, setUserRole] = useState('EMPLOYEE');
  const [tab, setTab] = useState('commute');
  const [menuOpen, setMenuOpen] = useState(false);
  const [toast, setToast] = useState(null);
  const [sosActive, setSosActive] = useState(false);
  const [locationShared, setLocationShared] = useState(false);
  const [currentTime, setCurrentTime] = useState(new Date());
  const [showMap, setShowMap] = useState(false);
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
        api.get('/api/alerts/my')
      ]);
      setRoutes(Array.isArray(r.data) ? r.data : []);
      setMyBookings(Array.isArray(b.data) ? b.data : []);
      setMyAlerts(Array.isArray(a.data) ? a.data : []);
      
      // Fetch analytics only if user is NOT a citizen/employee who doesn't need global stats
      // (Though currently we don't show global stats to them anymore)
    } catch (e) { console.error(e); }
    finally { setLoading(false); }
  }, []);

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) { navigate('/login'); return; }
    try { 
      const u = parseJwt(token); 
      if (u?.sub) setUserName(u.sub.split('@')[0]); 
      if (u?.role) {
        setUserRole(u.role);
        if (u.role === 'EMPLOYEE' && tab === 'commute') {
          setTab('history');
        }
      }
    } catch (e) {}
    fetchData();
    pollRef.current = setInterval(fetchData, POLL_INTERVAL);
    const clock = setInterval(() => setCurrentTime(new Date()), 1000);
    return () => { clearInterval(pollRef.current); clearInterval(clock); };
  }, [navigate, fetchData]);

  const [bookingModal, setBookingModal] = useState(null); // stores the route to book
  const [seatCount, setSeatCount] = useState(1);
  const [passengerDetails, setPassengerDetails] = useState('');

  const handleBook = async () => {
    if (!bookingModal) return;
    setBookingLoading(bookingModal.id);
    try { 
      await api.post('/api/booking', { 
        routeId: bookingModal.id,
        numberOfSeats: seatCount,
        passengerDetails: passengerDetails
      }); 
      await fetchData(); 
      showToast(`Successfully booked ${seatCount} seat(s)! 🎉`); 
      setBookingModal(null);
      setSeatCount(1);
      setPassengerDetails('');
    }
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

  const [supportMessage, setSupportMessage] = useState('');
  const [supportLoading, setSupportLoading] = useState(false);

  const handleSupportSubmit = async (e) => {
    e.preventDefault();
    if (!supportMessage.trim()) return;
    setSupportLoading(true);
    try {
      await api.post('/api/alerts', { type: 'QUERY', message: supportMessage, location: 'Help Desk' });
      showToast('Query submitted successfully! Admin will respond soon.');
      setSupportMessage('');
    } catch (err) {
      showToast('Failed to submit query.', 'error');
    } finally {
      setSupportLoading(false);
    }
  };

  const navItems = [
    ...(userRole === 'CITIZEN' ? [{ id: 'commute', label: 'Commute', icon: MapPin }] : []),
    { id: 'history', label: 'My Rides', icon: History },
    { id: 'safety', label: 'Safety Hub', icon: Shield },
    { id: 'support', label: 'Help Desk', icon: MessageSquare },
    ...(userRole === 'EMPLOYEE' ? [{ id: 'analytics', label: 'Analytics', icon: Zap }] : [])
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
              <h1 className="page-title">Hi, {userName}</h1>
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
                          <span style={{ display:'flex', alignItems:'center', gap:'0.35rem' }}><Star size={13} style={{ color:'#f59e0b' }} />{seatsLeft} seats</span>
                          <span style={{ fontWeight: 800, color: '#10b981' }}>${route.budget || '0.00'} / Trip</span>
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

                        <button onClick={() => !alreadyBooked && !isFull && setBookingModal(route)}
                          disabled={isFull || alreadyBooked || bookingLoading === route.id}
                          style={{ width:'100%', padding:'0.75rem', borderRadius:12, border:'none', cursor: isFull||alreadyBooked ? 'not-allowed':'pointer',
                            background: alreadyBooked ? 'rgba(16,185,129,0.1)' : isFull ? 'rgba(0,0,0,0.04)' : '#111827',
                            color: alreadyBooked ? '#10b981' : isFull ? '#64748b' : 'white',
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

          {/* SUPPORT TAB */}
          {tab === 'support' && (
            <motion.div initial={{ opacity:0, y:16 }} animate={{ opacity:1, y:0 }}>
              <div style={{ marginBottom:'1.75rem' }}>
                <h3 style={{ fontSize:'1.75rem', fontWeight:900, letterSpacing:'-0.03em' }}>Help Desk</h3>
                <p className="section-sub">Submit your queries or issues to the administration</p>
              </div>

              <div className="glass-card" style={{ marginBottom: '2rem' }}>
                <div className="flex-between" style={{ marginBottom: '1.5rem' }}>
                    <h4 style={{ fontWeight: 800, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                        <MapPin size={18} style={{ color: '#6366f1' }} /> Transit Network Map
                    </h4>
                    <button className="uber-btn-plain" onClick={() => setShowMap(!showMap)}>
                        {showMap ? 'Hide Map' : 'View Network'}
                    </button>
                </div>

                <AnimatePresence>
                    {showMap && (
                        <motion.div initial={{ height: 0, opacity: 0 }} animate={{ height: '350px', opacity: 1 }} exit={{ height: 0, opacity: 0 }} style={{ overflow: 'hidden', position: 'relative', borderRadius: '16px', background: '#0f172a', border: '1px solid rgba(255,255,255,0.1)' }}>
                            <div style={{ position: 'absolute', top: '1rem', left: '1rem', zIndex: 10, width: '240px' }}>
                                <div className="input-container" style={{ background: 'rgba(15,23,42,0.8)', backdropFilter: 'blur(8px)' }}>
                                    <Search size={14} style={{ marginLeft: '0.75rem' }} />
                                    <input 
                                        className="uber-input-plain" 
                                        placeholder="Search locations..." 
                                        style={{ height: '36px', fontSize: '0.8rem' }}
                                        value={mapSearch}
                                        onChange={e => setMapSearch(e.target.value)}
                                    />
                                </div>
                            </div>
                            
                            {/* Simulated Map Canvas */}
                            <div style={{ width: '100%', height: '100%', position: 'relative', overflow: 'hidden' }}>
                                <div style={{ position: 'absolute', inset: 0, opacity: 0.1, backgroundImage: 'radial-gradient(circle, #6366f1 1px, transparent 1px)', backgroundSize: '30px 30px' }} />
                                
                                {/* Route Lines */}
                                <svg style={{ position: 'absolute', inset: 0, width: '100%', height: '100%' }}>
                                    <line x1="20%" y1="30%" x2="50%" y2="50%" stroke="rgba(99,102,241,0.3)" strokeWidth="2" strokeDasharray="5,5" />
                                    <line x1="80%" y1="20%" x2="50%" y2="50%" stroke="rgba(99,102,241,0.3)" strokeWidth="2" strokeDasharray="5,5" />
                                    <line x1="40%" y1="80%" x2="50%" y2="50%" stroke="rgba(99,102,241,0.3)" strokeWidth="2" strokeDasharray="5,5" />
                                </svg>

                                {/* Markers */}
                                {[
                                    { x: '20%', y: '30%', name: 'Terminal A' },
                                    { x: '80%', y: '20%', name: 'IT Park' },
                                    { x: '40%', y: '80%', name: 'Suburbs' },
                                    { x: '50%', y: '50%', name: 'City Center', main: true }
                                ].map((marker, i) => (
                                    <motion.div key={i} initial={{ scale: 0 }} animate={{ scale: 1 }} transition={{ delay: i * 0.1 }}
                                        style={{ position: 'absolute', left: marker.x, top: marker.y, transform: 'translate(-50%, -50%)', textAlign: 'center' }}>
                                        <div style={{ width: marker.main ? 24 : 16, height: marker.main ? 24 : 16, borderRadius: '50%', background: marker.main ? '#6366f1' : '#10b981', margin: '0 auto', boxShadow: `0 0 15px ${marker.main ? '#6366f1' : '#10b981'}` }} />
                                        <p style={{ fontSize: '0.65rem', fontWeight: 800, marginTop: '4px', whiteSpace: 'nowrap', textShadow: '0 2px 4px black' }}>{marker.name}</p>
                                    </motion.div>
                                ))}
                            </div>

                            <div style={{ position: 'absolute', bottom: '1rem', right: '1rem', zIndex: 10 }}>
                                <button className="uber-btn uber-btn-accent" style={{ fontSize: '0.75rem', padding: '0.5rem 1rem' }} onClick={() => {
                                    setSupportMessage(`Requesting a new route from: ${mapSearch || 'Current Selection'} to IT Park`);
                                    setTab('support');
                                }}>
                                    Request Route Here
                                </button>
                            </div>
                        </motion.div>
                    )}
                </AnimatePresence>
              </div>

              <div className="glass-card">
                <form onSubmit={handleSupportSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                  <label style={{ fontWeight: 700, fontSize: '0.875rem' }}>Message Admin</label>
                  <textarea 
                    className="uber-input-plain" 
                    placeholder="Describe your issue, query, or route request here..." 
                    rows={4} 
                    value={supportMessage}
                    onChange={(e) => setSupportMessage(e.target.value)}
                    style={{ resize: 'vertical' }}
                    required 
                  />
                  <button 
                    type="submit" 
                    className="uber-btn uber-btn-accent" 
                    disabled={supportLoading}
                    style={{ width: 'auto', padding: '0.75rem 2rem', alignSelf: 'flex-start', display: 'flex', gap: '0.5rem', alignItems: 'center' }}
                  >
                    {supportLoading ? <RefreshCw size={16} style={{ animation: 'spin 1s linear infinite' }} /> : <Send size={16} />}
                    Submit Query
                  </button>
                </form>
              </div>

              {/* User Queries & Responses */}
              <div style={{ marginTop: '2rem' }}>
                <h4 style={{ fontWeight: 800, marginBottom: '1rem' }}>My Recent Queries</h4>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                    {myAlerts.map(a => (
                        <div key={a.id} className="glass-card" style={{ borderLeft: `4px solid ${a.resolved ? '#10b981' : '#f59e0b'}` }}>
                            <div className="flex-between" style={{ marginBottom: '0.5rem' }}>
                                <span className={`badge ${a.type === 'SOS' ? 'badge-error' : 'badge-info'}`}>{a.type}</span>
                                <span style={{ fontSize: '0.7rem', color: '#64748b' }}>{new Date(a.timestamp).toLocaleDateString()}</span>
                            </div>
                            <p style={{ fontSize: '0.875rem', fontWeight: 600, marginBottom: '0.75rem' }}>{a.message}</p>
                            
                            {a.adminResponse ? (
                                <div style={{ background: 'rgba(16,185,129,0.05)', padding: '0.875rem', borderRadius: '12px', border: '1px solid rgba(16,185,129,0.1)' }}>
                                    <p style={{ fontSize: '0.7rem', fontWeight: 900, color: '#10b981', textTransform: 'uppercase', marginBottom: '0.25rem' }}>Admin Response</p>
                                    <p style={{ fontSize: '0.825rem', color: '#e2e8f0' }}>{a.adminResponse}</p>
                                    <p style={{ fontSize: '0.65rem', color: '#64748b', marginTop: '0.5rem' }}>Responded on: {new Date(a.respondedAt).toLocaleString()}</p>
                                </div>
                            ) : (
                                <p style={{ fontSize: '0.75rem', color: '#94a3b8', fontStyle: 'italic' }}>Awaiting response from administration...</p>
                            )}
                        </div>
                    ))}
                    {myAlerts.length === 0 && <p style={{ textAlign: 'center', opacity: 0.4, padding: '2rem' }}>No queries submitted yet.</p>}
                </div>
              </div>
            </motion.div>
          )}

          {/* ANALYTICS TAB */}
          {tab === 'analytics' && (
            <motion.div initial={{ opacity:0, y:16 }} animate={{ opacity:1, y:0 }}>
              <div style={{ marginBottom:'1.75rem' }}>
                <h3 style={{ fontSize:'1.75rem', fontWeight:900, letterSpacing:'-0.03em' }}>My Work & Earnings</h3>
                <p className="section-sub">Track your transport usage and savings</p>
              </div>

              <div className="grid-3" style={{ marginBottom: '1.5rem' }}>
                <div className="glass-card">
                  <h4 style={{ color: '#64748b', fontSize: '0.875rem' }}>Total Commutes</h4>
                  <p style={{ fontSize: '2rem', fontWeight: 900, color: '#6366f1' }}>{myBookings.length}</p>
                </div>
                <div className="glass-card">
                  <h4 style={{ color: '#64748b', fontSize: '0.875rem' }}>Money Saved (Est.)</h4>
                  <p style={{ fontSize: '2rem', fontWeight: 900, color: '#10b981' }}>${myBookings.reduce((acc, b) => acc + (b.route?.budget || 12), 0)}</p>
                </div>
                <div className="glass-card">
                  <h4 style={{ color: '#64748b', fontSize: '0.875rem' }}>Carbon Reduced</h4>
                  <p style={{ fontSize: '2rem', fontWeight: 900, color: '#0ea5e9' }}>{(myBookings.length * 2.4).toFixed(1)}kg</p>
                </div>
              </div>

              <div className="glass-card" style={{ padding: '2rem', height: '300px' }}>
                <h4 style={{ fontWeight: 800, marginBottom: '1.5rem' }}>Commute Frequency</h4>
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={[
                    { name: 'Mon', uv: myBookings.length > 0 ? 2 : 0 },
                    { name: 'Tue', uv: myBookings.length > 0 ? 3 : 0 },
                    { name: 'Wed', uv: myBookings.length > 0 ? 1 : 0 },
                    { name: 'Thu', uv: myBookings.length > 0 ? 4 : 0 },
                    { name: 'Fri', uv: myBookings.length > 0 ? 2 : 0 }
                  ]}>
                    <XAxis dataKey="name" stroke="#94a3b8" />
                    <YAxis stroke="#94a3b8" />
                    <Tooltip cursor={{fill: 'rgba(255,255,255,0.05)'}} contentStyle={{backgroundColor: '#1e293b', border: 'none', borderRadius: '8px'}} />
                    <Bar dataKey="uv" fill="#6366f1" radius={[4, 4, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            </motion.div>
          )}

          {/* BOOKING MODAL */}
          <AnimatePresence>
            {bookingModal && (
              <div className="modal-overlay" onClick={() => setBookingModal(null)}>
                <motion.div 
                  initial={{ opacity: 0, scale: 0.9, y: 20 }}
                  animate={{ opacity: 1, scale: 1, y: 0 }}
                  exit={{ opacity: 0, scale: 0.9, y: 20 }}
                  className="modal-card" 
                  onClick={e => e.stopPropagation()}
                >
                  <div className="flex-between" style={{ marginBottom: '1.5rem' }}>
                    <h3 style={{ fontWeight: 900, fontSize: '1.25rem' }}>Confirm Booking</h3>
                    <button onClick={() => setBookingModal(null)} style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#64748b' }}>
                      <X size={20} />
                    </button>
                  </div>

                  <div style={{ background: 'rgba(99,102,241,0.05)', borderRadius: '12px', padding: '1rem', border: '1px solid rgba(99,102,241,0.1)', marginBottom: '1.5rem' }}>
                    <p style={{ fontSize: '0.75rem', fontWeight: 800, color: '#6366f1', textTransform: 'uppercase', marginBottom: '0.25rem' }}>Route Details</p>
                    <p style={{ fontWeight: 700 }}>{bookingModal.source || 'Main Hub'} → {bookingModal.destination}</p>
                    <p style={{ fontSize: '0.875rem', color: '#64748b' }}>Scheduled for {bookingModal.pickupTime}</p>
                  </div>

                  <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
                    <div className="input-group">
                      <label className="input-label">Number of Seats</label>
                      <select 
                        className="uber-input-plain"
                        value={seatCount}
                        onChange={e => setSeatCount(parseInt(e.target.value))}
                      >
                        {[1, 2, 3, 4, 5].map(n => (
                          <option key={n} value={n}>{n} Seat{n > 1 ? 's' : ''}</option>
                        ))}
                      </select>
                    </div>

                    {seatCount > 1 && (
                      <motion.div initial={{ opacity: 0, height: 0 }} animate={{ opacity: 1, height: 'auto' }} className="input-group">
                        <label className="input-label">Passenger Details</label>
                        <textarea 
                          className="uber-input-plain"
                          placeholder="Enter names or details of other passengers..."
                          rows={3}
                          value={passengerDetails}
                          onChange={e => setPassengerDetails(e.target.value)}
                        />
                      </motion.div>
                    )}

                    <div style={{ marginTop: '1rem', paddingTop: '1rem', borderTop: '1px solid var(--border-glass)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <div>
                        <p style={{ fontSize: '0.75rem', color: '#64748b' }}>Total Cost</p>
                        <p style={{ fontSize: '1.25rem', fontWeight: 900, color: '#10b981' }}>${(bookingModal.budget * seatCount).toFixed(2)}</p>
                      </div>
                      <button 
                        className="uber-btn uber-btn-accent" 
                        style={{ width: 'auto', padding: '0.875rem 2rem' }}
                        onClick={handleBook}
                        disabled={bookingLoading}
                      >
                        {bookingLoading ? <RefreshCw size={18} className="animate-spin" /> : 'Confirm & Pay'}
                      </button>
                    </div>
                  </div>
                </motion.div>
              </div>
            )}
          </AnimatePresence>

        </div>
      </main>
    </div>
  );
}
