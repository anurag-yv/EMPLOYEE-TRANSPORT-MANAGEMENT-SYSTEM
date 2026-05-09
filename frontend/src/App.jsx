import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import AdminDashboard from './pages/AdminDashboard';
import { parseJwt } from './utils/jwt';
import './index.css';

// Protected Route Component
const ProtectedRoute = ({ children, roleRequired }) => {
  const token = localStorage.getItem('token');
  if (!token) {
    return <Navigate to="/login" replace />;
  }
  
  try {
    const user = parseJwt(token);
    if (roleRequired && user.role !== roleRequired) {
      return <Navigate to={user.role === 'ADMIN' ? '/admin' : '/dashboard'} replace />;
    }
    return children;
  } catch (e) {
    localStorage.removeItem('token');
    return <Navigate to="/login" replace />;
  }
};

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        
        {/* Employee Routes */}
        <Route 
          path="/dashboard" 
          element={
            <ProtectedRoute roleRequired="EMPLOYEE">
              <Dashboard />
            </ProtectedRoute>
          } 
        />

        {/* Admin Routes */}
        <Route 
          path="/admin" 
          element={
            <ProtectedRoute roleRequired="ADMIN">
              <AdminDashboard />
            </ProtectedRoute>
          } 
        />

        <Route path="/" element={<Navigate to="/login" replace />} />
      </Routes>
    </Router>
  );
}

export default App;
