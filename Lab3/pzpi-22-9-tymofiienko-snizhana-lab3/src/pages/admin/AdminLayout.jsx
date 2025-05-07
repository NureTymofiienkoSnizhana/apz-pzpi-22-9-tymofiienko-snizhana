import React from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import './AdminLayout.css';
import petHouseLogo from '../../assets/pet-house.png';

const AdminLayout = () => {
  const navigate = useNavigate();
  
  const handleLogout = () => {
    navigate('/login');
  };

  return (
    <div className="admin-layout">
      <header className="admin-header">
        <div className="header-logo">
          <img src={petHouseLogo} alt="PetHealth Logo" className="logo" />
          <h1>PetHealth</h1>
        </div>
        
        <nav className="main-nav">
          <NavLink to="/admin/pets" className={({ isActive }) => 
            isActive ? "nav-link active" : "nav-link"}>
            Тварини
          </NavLink>
          <NavLink to="/admin/users" className={({ isActive }) => 
            isActive ? "nav-link active" : "nav-link"}>
            Користувачі
          </NavLink>
          <NavLink to="/admin/devices" className={({ isActive }) => 
            isActive ? "nav-link active" : "nav-link"}>
            Пристрої
          </NavLink>
        </nav>
        
        <div className="user-controls">
          <NavLink to="/admin/profile" className={({ isActive }) => 
            isActive ? "profile-button active" : "profile-button"}>
            Мій профіль
          </NavLink>
          <button onClick={handleLogout} className="logout-button">
            Вихід
          </button>
        </div>
      </header>
      
      <main className="admin-content">
        <Outlet />
      </main>
      
      <footer className="admin-footer">
        <p>© 2025 PetHealth. Всі права захищені.</p>
      </footer>
    </div>
  );
};

export default AdminLayout;