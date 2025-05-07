import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import Login from './pages/auth/Login';
import ForgotPassword from './pages/auth/ForgotPassword';
import Unauthorized from './components/common/Unauthorized';
import ProtectedRoute from './components/common/ProtectedRoute';
import AdminLayout from './pages/admin/AdminLayout'; 
import PetsManagement from './pages/admin/PetsManagement';
import UsersManagement from './pages/admin/UsersManagement';
import DevicesManagement from './pages/admin/DevicesManagement';
import UserProfile from './pages/profile/UserProfile';
import Logout from './pages/auth/Logout';
import VetLayout from './pages/vet/VetLayout';
import VetPetsManagement from './pages/vet/VetPetsManagement';
import './App.css';

function App() {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          {/* Публічні маршрути */}
          <Route path="/login" element={<Login />} />
          <Route path="/forgot-password" element={<ForgotPassword />} />
          <Route path="/unauthorized" element={<Unauthorized />} />
          <Route path="/logout" element={<Logout />} />
          
          {/* Захищені маршрути для адміністратора */}
          <Route path="/admin" element={<ProtectedRoute allowedRoles={['admin']} />}>
            <Route element={<AdminLayout />}>
              <Route path="pets" element={<PetsManagement />} />
              <Route path="users" element={<UsersManagement />} />
              <Route path="devices" element={<DevicesManagement />} />
              <Route path="profile" element={<UserProfile />} />
              <Route path="dashboard" element={<Navigate to="/admin/pets" replace />} />
              <Route path="" element={<Navigate to="/admin/pets" replace />} />
            </Route>
          </Route>
          
          {/* Захищені маршрути для ветеринара */}
          <Route path="/vet" element={<ProtectedRoute allowedRoles={['vet']} />}>
            <Route element={<VetLayout />}> 
              <Route path="pets" element={<VetPetsManagement />} />
              <Route path="profile" element={<UserProfile />} />
              <Route path="dashboard" element={<Navigate to="/vet/pets" replace />} />
            </Route>
          </Route>
          
          {/* Редирект на сторінку логіну */}
          <Route path="/" element={<Navigate to="/login" replace />} />
          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </Router>
    </AuthProvider>
  );
}

export default App;